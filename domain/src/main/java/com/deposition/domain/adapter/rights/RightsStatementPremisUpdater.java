package com.deposition.domain.adapter.rights;

import com.deposition.domain.dto.schema.premis.v3.*;
import com.deposition.domain.dto.schema.premis.v3.converter.AgentConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.EventConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisRightsStatementConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.RightsStatementConverter;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.RightsStatementMetadata;
import com.deposition.domain.models.enums.*;
import com.deposition.domain.models.valueobject.*;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest.AgentGrant;
import com.deposition.domain.port.out.UserOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public final class RightsStatementPremisUpdater {

    private final AgentConverter agentConverter;
    private final EventConverter eventConverter;
    private final RightsStatementConverter rightsStatementConverter;
    private final PremisRightsStatementConverter premisRightsStatementConverter;
    private final UserOutPort userOutPort;

    private static List<EventAgentLink> buildAgentLinks(String authenticactedUserId) {
        var agentIdentifier = new AgentIdentifier(AgentIdentifierType.SYSTEM, authenticactedUserId.toString());
        return List.of(new EventAgentLink(agentIdentifier, List.of(EventAgentLinkRole.AUTHORIZER)));
    }

    private static RightsStatementComplexType findExistingRightsStatement(RightsComplexType rights, String rightsStatementId) {
        if (rights == null || rights.getRightsStatementOrRightsExtension() == null) {
            return null;
        }
        for (var item : rights.getRightsStatementOrRightsExtension()) {
            if (!(item instanceof RightsStatementComplexType rs)) {
                continue;
            }
            if (Objects.equals(extractRightsStatementIdentifierValue(rs), rightsStatementId)) {
                return rs;
            }
        }
        return null;
    }

    private static void applyPayload(RightsStatementMetadata model, UpsertRightsStatementRequest.RightsStatementPayload payload) {
        if (model == null || payload == null) {
            return;
        }

        if (payload.copyrightInformation() != null) {
            model.setCopyrightInformation(List.of(payload.copyrightInformation()));
        }
        if (payload.licenseInformation() != null) {
            model.setLicenseInformation(List.of(payload.licenseInformation()));
        }
        if (payload.otherRightsInformation() != null) {
            model.setOtherRightsInformation(payload.otherRightsInformation());
        }
        if (payload.statuteInformation() != null) {
            model.setStatuteInformation(new ArrayList<>(payload.statuteInformation()));
        }
        if (payload.rightsGranted() != null) {
            model.setRightsGranted(new ArrayList<>(payload.rightsGranted()));
        }
    }

    private static String extractRightsStatementIdentifierValue(RightsStatementComplexType rs) {
        if (rs == null || rs.getRightsStatementIdentifier() == null) {
            return null;
        }
        return rs.getRightsStatementIdentifier().getRightsStatementIdentifierValue();
    }

    private static String resolveAgentId(AgentGrant grant) {
        if (grant == null || grant.agent() == null) {
            return null;
        }
        if (grant.agent().id() != null && !grant.agent().id().isBlank()) {
            return grant.agent().id();
        }
        return null;
    }

    private static String toXmlId(String id) {
        return "id_" + id;
    }

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

        RightsComplexType rights = findOrCreateRightsContainer(premis, request.rightsStatementId());

        RightsStatementComplexType rightsStatement = upsertRightsStatement(rights, objectId, request);

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
        var authenticatedUserId = userOutPort.getCurrentUserId();

        var detail = "RightsStatement updated via REST API: rightsStatementId=" + rightsStatementId;

        var event = EventMetadata.builder()
                .id(eventId)
                .identifier(new EventIdentifier(EventIdentifierType.SYSTEM, eventId.toString()))
                .type(EventType.METADATA_MODIFICATION)
                .dateTime(OffsetDateTime.now())
                .detail(List.of(new EventDetailInformation(detail)))
                .objectLinks(List.of(new EventObjectLink(
                        new ObjectIdentifier(ObjectIdentifierType.SYSTEM, objectId.toString()),
                        List.of(EventObjectLinkRole.OUTCOME))))
                .agentLinks(buildAgentLinks(authenticatedUserId))
                .build();

        premis.getEvent().add(eventConverter.map(event));
        ensureCurrentUserAgentPresent(premis, authenticatedUserId);
    }

    private void ensureCurrentUserAgentPresent(PremisComplexType premis, String authenticactedUserId) {
        var agentXmlId = toXmlId(authenticactedUserId);
        if (premis.getAgent() != null && premis.getAgent().stream()
                .filter(Objects::nonNull)
                .anyMatch(agent -> Objects.equals(agentXmlId, agent.getXmlID()))) {
            return;
        }

        var agent = AgentMetadata.builder()
                .id(authenticactedUserId)
                .type(AgentType.PERSON)
                .identifiers(List.of(AgentIdentifier.builder()
                        .type(AgentIdentifierType.SYSTEM)
                        .value(authenticactedUserId)
                        .build()))
                .build();

        premis.getAgent().add(agentConverter.map(agent));
    }

    private RightsComplexType findOrCreateRightsContainer(PremisComplexType premis, String rightsStatementId) {
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

    private RightsStatementComplexType upsertRightsStatement(
            RightsComplexType rights,
            UUID objectId,
            UpsertRightsStatementRequest request) {
        RightsStatementComplexType existing = findExistingRightsStatement(rights, request.rightsStatementId());

        RightsStatementMetadata model;
        if (existing != null) {
            model = premisRightsStatementConverter.map(existing);
        } else {
            model = RightsStatementMetadata.builder().build();
        }

        model.setId(request.rightsStatementId());
        model.setRightsBasis(request.rightsBasis().name());

        if (request.payload() != null) {
            applyPayload(model, request.payload());
        }

        var rs = rightsStatementConverter.map(model);

        rs.getLinkingObjectIdentifier().clear();
        rs.getLinkingObjectIdentifier().add(buildLinkingObjectIdentifier(objectId));

        if (request.agents() != null) {
            Map<String, LinkedHashSet<String>> rolesByAgentId = new TreeMap<>();

            for (var grant : request.agents()) {
                if (grant == null) {
                    continue;
                }
                String agentId = resolveAgentId(grant);
                if (agentId == null) {
                    continue;
                }
                rolesByAgentId.computeIfAbsent(agentId, __ -> new LinkedHashSet<>());
                if (grant.linkingAgentRoles() != null) {
                    for (var r : grant.linkingAgentRoles()) {
                        if (r == null || r.isBlank()) {
                            continue;
                        }
                        rolesByAgentId.get(agentId).add(r);
                    }
                }
            }

            rs.getLinkingAgentIdentifier().clear();
            for (var e : rolesByAgentId.entrySet()) {
                rs.getLinkingAgentIdentifier().add(buildLinkingAgentIdentifier(e.getKey(), List.copyOf(e.getValue())));
            }
        }

        return rs;
    }

    private LinkingObjectIdentifierComplexType buildLinkingObjectIdentifier(UUID objectId) {
        var link = new LinkingObjectIdentifierComplexType();
        link.setLinkingObjectIdentifierType(toStringPlusAuthority(ObjectIdentifierType.SYSTEM.name()));
        link.setLinkingObjectIdentifierValue(objectId.toString());
        link.getLinkingObjectRole().add(toStringPlusAuthority("SUBJECT"));
        return link;
    }

    private LinkingAgentIdentifierComplexType buildLinkingAgentIdentifier(String agentId, List<String> roles) {
        var link = new LinkingAgentIdentifierComplexType();
        link.setLinkingAgentIdentifierType(toStringPlusAuthority(AgentIdentifierType.SYSTEM.name()));
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

    private StringPlusAuthority toStringPlusAuthority(String value) {
        var out = new StringPlusAuthority();
        out.setValue(value);
        return out;
    }

}
