package com.deposition.domain.adapter.rights;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.deposition.domain.dto.schema.premis.v3.LinkingAgentIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.LinkingObjectIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectFactory;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.RightsComplexType;
import com.deposition.domain.dto.schema.premis.v3.RightsGrantedComplexType;
import com.deposition.domain.dto.schema.premis.v3.RightsStatementComplexType;
import com.deposition.domain.dto.schema.premis.v3.RightsStatementIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.StartAndEndDateComplexType;
import com.deposition.domain.dto.schema.premis.v3.StringPlusAuthority;
import com.deposition.domain.dto.schema.premis.v3.converter.EventConverter;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.enums.EventAgentLinkRole;
import com.deposition.domain.models.enums.EventIdentifierType;
import com.deposition.domain.models.enums.EventObjectLinkRole;
import com.deposition.domain.models.enums.EventType;
import com.deposition.domain.models.enums.ObjectIdentifierType;
import com.deposition.domain.models.valueobject.AgentIdentifier;
import com.deposition.domain.models.valueobject.EventAgentLink;
import com.deposition.domain.models.valueobject.EventDetailInformation;
import com.deposition.domain.models.valueobject.EventIdentifier;
import com.deposition.domain.models.valueobject.EventObjectLink;
import com.deposition.domain.models.valueobject.Identifier;
import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest.AgentGrant;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class RightsStatementPremisUpdater {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private final com.deposition.domain.dto.schema.premis.v3.converter.AgentConverter agentConverter;
    private final com.deposition.domain.dto.schema.premis.v3.converter.CommonConverter commonConverter;
    private final EventConverter eventConverter;

    /**
     * Upserts a rights statement in PREMIS root and links it to the specified
     * object. Also ensures referenced agents are present and links them to the
     * rights statement.
     */
    public void upsertRightsStatement(PremisComplexType premis, UUID objectId, UpsertRightsStatementRequest request,
            List<AgentMetadata> agentsToEnsure) {
        if (premis == null) {
            throw new IllegalArgumentException("premis must not be null");
        }
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        if (agentsToEnsure != null) {
            for (var agent : agentsToEnsure) {
                ensureAgentPresent(premis, agent);
            }
        }

        RightsStatementComplexType rightsStatement = buildRightsStatement(objectId, request);

        RightsComplexType rights = findOrCreateRightsContainer(premis, request.rightsStatementId());

        // Replace existing rightsStatement with same identifier value, otherwise add.
        var items = rights.getRightsStatementOrRightsExtension();
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (!(item instanceof RightsStatementComplexType existing)) {
                continue;
            }
            var existingId = extractRightsStatementIdentifierValue(existing);
            if (Objects.equals(existingId, request.rightsStatementId())) {
                items.set(i, rightsStatement);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            items.add(rightsStatement);
        }

        addRightsStatementUpdateEvent(premis, objectId, request.rightsStatementId());
    }

    private void addRightsStatementUpdateEvent(PremisComplexType premis, UUID objectId, String rightsStatementId) {
        var eventId = UUID.randomUUID();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        var detail = "RightsStatement updated via REST API: rightsStatementId=" + rightsStatementId;

        var event = EventMetadata.builder()
                .id(eventId)
                .identifier(new EventIdentifier(EventIdentifierType.LOCAL, eventId.toString()))
                .type(EventType.METADATA_MODIFICATION)
                .dateTime(OffsetDateTime.now())
                .detail(List.of(new EventDetailInformation(detail)))
                .objectLinks(List.of(new EventObjectLink(
                        new ObjectIdentifier(ObjectIdentifierType.LOCAL, objectId.toString()),
                        List.of(EventObjectLinkRole.OUTCOME))))
                .agentLinks(buildAgentLinks(authentication))
                .build();

        // Use Spring-managed converter; ensure current user agent is present.
        premis.getEvent().add(eventConverter.map(event));
        ensureCurrentUserAgentPresent(premis, authentication);
    }

    private static List<EventAgentLink> buildAgentLinks(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        var agentIdentifier = new AgentIdentifier(AgentIdentifierType.LOCAL, authentication.getName());
        return List.of(new EventAgentLink(agentIdentifier, List.of(EventAgentLinkRole.AUTHORIZER)));
    }

    private void ensureCurrentUserAgentPresent(PremisComplexType premis, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        String agentId = authentication.getName();
        if (agentId == null || agentId.isBlank()) {
            return;
        }

        var agentXmlId = toXmlId(agentId);
        if (premis.getAgent() != null && premis.getAgent().stream()
                .filter(Objects::nonNull)
                .anyMatch(agent -> Objects.equals(agentXmlId, agent.getXmlID()))) {
            return;
        }

        var agent = AgentMetadata.builder()
                .id(agentId)
                .name(agentId)
                .type(com.deposition.domain.models.enums.AgentType.PERSON)
                .identifiers(List.of(Identifier.builder()
                        .type(ObjectIdentifierType.LOCAL.name())
                        .value(agentId)
                        .build()))
                .build();

        premis.getAgent().add(agentConverter.map(agent));
    }

    private RightsComplexType findOrCreateRightsContainer(PremisComplexType premis, String rightsStatementId) {
        // We keep a single rights container per statement id (xmlID = id_{rightsStatementId}).
        String xmlId = toXmlId(rightsStatementId);
        for (var r : premis.getRights()) {
            if (r != null && Objects.equals(xmlId, r.getXmlID())) {
                return r;
            }
        }

        var rights = new RightsComplexType();
        rights.setXmlID(xmlId);
        rights.setVersion("3.0");
        premis.getRights().add(rights);
        return rights;
    }

    private RightsStatementComplexType buildRightsStatement(UUID objectId, UpsertRightsStatementRequest request) {
        var rs = new RightsStatementComplexType();

        var id = new RightsStatementIdentifierComplexType();
        id.setRightsStatementIdentifierType(toStringPlusAuthority("LOCAL"));
        id.setRightsStatementIdentifierValue(request.rightsStatementId());
        rs.setRightsStatementIdentifier(id);

        rs.setRightsBasis(toStringPlusAuthority(request.rightsBasis().name()));

        if (request.payload() != null) {
            applyPayload(rs, request.payload());
        }

        // Always link to target object.
        rs.getLinkingObjectIdentifier().add(buildLinkingObjectIdentifier(objectId));

        if (request.agents() != null) {
            for (var grant : request.agents()) {
                if (grant == null) {
                    continue;
                }
                String agentId = resolveAgentId(grant);
                if (agentId == null) {
                    continue;
                }
                rs.getLinkingAgentIdentifier().add(buildLinkingAgentIdentifier(agentId, grant.linkingAgentRoles()));
            }
        }

        return rs;
    }

    private void applyPayload(RightsStatementComplexType rs, UpsertRightsStatementRequest.RightsStatementPayload payload) {
        if (payload.copyrightInformation() != null) {
            rs.setCopyrightInformation(mapCopyright(payload.copyrightInformation()));
        }
        if (payload.licenseInformation() != null) {
            rs.setLicenseInformation(mapLicense(payload.licenseInformation()));
        }
        if (payload.otherRightsInformation() != null) {
            rs.setOtherRightsInformation(mapOtherRights(payload.otherRightsInformation()));
        }
        if (payload.statuteInformation() != null) {
            for (var st : payload.statuteInformation()) {
                if (st == null) {
                    continue;
                }
                rs.getStatuteInformation().add(mapStatute(st));
            }
        }
        if (payload.rightsGranted() != null) {
            for (var rg : payload.rightsGranted()) {
                if (rg == null) {
                    continue;
                }
                rs.getRightsGranted().add(mapRightsGranted(rg));
            }
        }
    }

    private com.deposition.domain.dto.schema.premis.v3.CopyrightInformationComplexType mapCopyright(
            com.deposition.domain.models.valueobject.CopyrightInformation in) {
        var out = new com.deposition.domain.dto.schema.premis.v3.CopyrightInformationComplexType();
        out.setCopyrightStatus(toStringPlusAuthority(in.getCopyrightStatus()));

        // Premis expects CountryCode, while domain stores string. We put it into value only.
        var country = new com.deposition.domain.dto.schema.premis.v3.CountryCode();
        country.setValue(in.getCopyrightJurisdiction());
        out.setCopyrightJurisdiction(country);

        if (in.getCopyrightStatusDeterminationDate() != null) {
            out.setCopyrightStatusDeterminationDate(in.getCopyrightStatusDeterminationDate().toString());
        }
        if (in.getCopyrightNote() != null) {
            out.getCopyrightNote().addAll(in.getCopyrightNote());
        }
        if (in.getApplicableDates() != null) {
            out.setCopyrightApplicableDates(mapDates(in.getApplicableDates().getStartDate(), in.getApplicableDates().getEndDate()));
        }
        if (in.getDocumentationIdentifiers() != null) {
            for (var d : in.getDocumentationIdentifiers()) {
                if (d == null) {
                    continue;
                }
                var doc = new com.deposition.domain.dto.schema.premis.v3.CopyrightDocumentationIdentifierComplexType();
                doc.setCopyrightDocumentationIdentifierType(toStringPlusAuthority(d.getType()));
                doc.setCopyrightDocumentationIdentifierValue(d.getValue());
                if (d.getRole() != null) {
                    doc.setCopyrightDocumentationRole(toStringPlusAuthority(d.getRole()));
                }
                out.getCopyrightDocumentationIdentifier().add(doc);
            }
        }
        return out;
    }

    private com.deposition.domain.dto.schema.premis.v3.LicenseInformationComplexType mapLicense(
            com.deposition.domain.models.valueobject.LicenseInformation in) {
        var out = new com.deposition.domain.dto.schema.premis.v3.LicenseInformationComplexType();

        // Due to schema choice / duplicated element names JAXB represents as "content" list.
        var content = out.getContent();
        if (in.getDocumentationIdentifiers() != null) {
            for (var d : in.getDocumentationIdentifiers()) {
                if (d == null) {
                    continue;
                }
                var doc = new com.deposition.domain.dto.schema.premis.v3.LicenseDocumentationIdentifierComplexType();
                doc.setLicenseDocumentationIdentifierType(toStringPlusAuthority(d.getType()));
                doc.setLicenseDocumentationIdentifierValue(d.getValue());
                if (d.getRole() != null) {
                    doc.setLicenseDocumentationRole(toStringPlusAuthority(d.getRole()));
                }
                content.add(OBJECT_FACTORY.createLicenseDocumentationIdentifier(doc));
            }
        }
        if (in.getLicenseTerms() != null) {
            content.add(OBJECT_FACTORY.createLicenseTerms(in.getLicenseTerms()));
        }
        if (in.getLicenseNote() != null) {
            for (var n : in.getLicenseNote()) {
                if (n == null) {
                    continue;
                }
                content.add(OBJECT_FACTORY.createLicenseNote(n));
            }
        }
        if (in.getApplicableDates() != null) {
            var dates = mapDates(in.getApplicableDates().getStartDate(), in.getApplicableDates().getEndDate());
            content.add(OBJECT_FACTORY.createLicenseApplicableDates(dates));
        }
        return out;
    }

    private com.deposition.domain.dto.schema.premis.v3.OtherRightsInformationComplexType mapOtherRights(
            com.deposition.domain.models.valueobject.OtherRightsInformation in) {
        var out = new com.deposition.domain.dto.schema.premis.v3.OtherRightsInformationComplexType();
        out.setOtherRightsBasis(toStringPlusAuthority(in.getOtherRightsBasis()));
        if (in.getApplicableDates() != null) {
            out.setOtherRightsApplicableDates(mapDates(in.getApplicableDates().getStartDate(), in.getApplicableDates().getEndDate()));
        }
        if (in.getOtherRightsNote() != null) {
            out.getOtherRightsNote().addAll(in.getOtherRightsNote());
        }
        if (in.getDocumentationIdentifiers() != null) {
            for (var d : in.getDocumentationIdentifiers()) {
                if (d == null) {
                    continue;
                }
                var doc = new com.deposition.domain.dto.schema.premis.v3.OtherRightsDocumentationIdentifierComplexType();
                doc.setOtherRightsDocumentationIdentifierType(toStringPlusAuthority(d.getType()));
                doc.setOtherRightsDocumentationIdentifierValue(d.getValue());
                if (d.getRole() != null) {
                    doc.setOtherRightsDocumentationRole(toStringPlusAuthority(d.getRole()));
                }
                out.getOtherRightsDocumentationIdentifier().add(doc);
            }
        }
        return out;
    }

    private com.deposition.domain.dto.schema.premis.v3.StatuteInformationComplexType mapStatute(
            com.deposition.domain.models.valueobject.StatuteInformation in) {
        var out = new com.deposition.domain.dto.schema.premis.v3.StatuteInformationComplexType();
        var country = new com.deposition.domain.dto.schema.premis.v3.CountryCode();
        country.setValue(in.getStatuteJurisdiction());
        out.setStatuteJurisdiction(country);
        out.setStatuteCitation(toStringPlusAuthority(in.getStatuteCitation()));
        if (in.getStatuteInformationDeterminationDate() != null) {
            out.setStatuteInformationDeterminationDate(in.getStatuteInformationDeterminationDate().toString());
        }
        if (in.getStatuteNote() != null) {
            out.getStatuteNote().addAll(in.getStatuteNote());
        }
        if (in.getApplicableDates() != null) {
            out.setStatuteApplicableDates(mapDates(in.getApplicableDates().getStartDate(), in.getApplicableDates().getEndDate()));
        }
        if (in.getDocumentationIdentifiers() != null) {
            for (var d : in.getDocumentationIdentifiers()) {
                if (d == null) {
                    continue;
                }
                var doc = new com.deposition.domain.dto.schema.premis.v3.StatuteDocumentationIdentifierComplexType();
                doc.setStatuteDocumentationIdentifierType(toStringPlusAuthority(d.getType()));
                doc.setStatuteDocumentationIdentifierValue(d.getValue());
                if (d.getRole() != null) {
                    doc.setStatuteDocumentationRole(toStringPlusAuthority(d.getRole()));
                }
                out.getStatuteDocumentationIdentifier().add(doc);
            }
        }
        return out;
    }

    private RightsGrantedComplexType mapRightsGranted(com.deposition.domain.models.valueobject.RightsGranted in) {
        var out = new RightsGrantedComplexType();
        out.setAct(toStringPlusAuthority(in.getAct()));
        if (in.getRestriction() != null) {
            for (var r : in.getRestriction()) {
                if (r == null) {
                    continue;
                }
                out.getRestriction().add(toStringPlusAuthority(r));
            }
        }
        if (in.getTermOfGrant() != null) {
            out.setTermOfGrant(mapDates(in.getTermOfGrant().getStartDate(), in.getTermOfGrant().getEndDate()));
        }
        if (in.getTermOfRestriction() != null) {
            out.setTermOfRestriction(mapDates(in.getTermOfRestriction().getStartDate(), in.getTermOfRestriction().getEndDate()));
        }
        if (in.getRightsGrantedNote() != null) {
            out.getRightsGrantedNote().addAll(in.getRightsGrantedNote());
        }
        return out;
    }

    private LinkingObjectIdentifierComplexType buildLinkingObjectIdentifier(UUID objectId) {
        var link = new LinkingObjectIdentifierComplexType();
        link.setLinkingObjectIdentifierType(toStringPlusAuthority(ObjectIdentifierType.LOCAL.name()));
        link.setLinkingObjectIdentifierValue(objectId.toString());
        link.getLinkingObjectRole().add(toStringPlusAuthority("SUBJECT"));
        return link;
    }

    private LinkingAgentIdentifierComplexType buildLinkingAgentIdentifier(String agentId, List<String> roles) {
        var link = new LinkingAgentIdentifierComplexType();
        link.setLinkingAgentIdentifierType(toStringPlusAuthority("LOCAL"));
        link.setLinkingAgentIdentifierValue(agentId);
        if (roles != null) {
            for (var r : roles) {
                if (r == null || r.isBlank()) {
                    continue;
                }
                link.getLinkingAgentRole().add(toStringPlusAuthority(r));
            }
        }
        return link;
    }

    private static String extractRightsStatementIdentifierValue(RightsStatementComplexType rs) {
        if (rs == null || rs.getRightsStatementIdentifier() == null) {
            return null;
        }
        return rs.getRightsStatementIdentifier().getRightsStatementIdentifierValue();
    }

    private void ensureAgentPresent(PremisComplexType premis, AgentMetadata agent) {
        if (premis == null || agent == null || agent.getId() == null || agent.getId().isBlank()) {
            return;
        }
        String xmlId = toXmlId(agent.getId());
        if (premis.getAgent() != null && premis.getAgent().stream()
                .filter(Objects::nonNull)
                .anyMatch(a -> Objects.equals(xmlId, a.getXmlID()))) {
            return;
        }
        premis.getAgent().add(agentConverter.map(agent));
    }

    private static String resolveAgentId(AgentGrant grant) {
        if (grant == null) {
            return null;
        }
        if (grant.userId() != null && !grant.userId().isBlank()) {
            return grant.userId();
        }
        if (grant.agent() != null && grant.agent().id() != null && !grant.agent().id().isBlank()) {
            return grant.agent().id();
        }
        return null;
    }

    private StringPlusAuthority toStringPlusAuthority(String value) {
        var out = new StringPlusAuthority();
        out.setValue(value);
        return out;
    }

    private static String toXmlId(String id) {
        return "id_" + id;
    }

    private static StartAndEndDateComplexType mapDates(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return null;
        }
        var out = new StartAndEndDateComplexType();
        out.setStartDate(start == null ? "" : start.toString());
        if (end != null) {
            out.setEndDate(end.toString());
        }
        return out;
    }
}
