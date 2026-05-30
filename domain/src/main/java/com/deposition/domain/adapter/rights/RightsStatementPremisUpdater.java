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
import com.deposition.domain.models.valueobject.RightsStatementAgentLink;
import com.deposition.domain.port.out.UserOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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

    private static String extractRightsStatementIdentifierValue(RightsStatementComplexType rs) {
        if (rs == null || rs.getRightsStatementIdentifier() == null) {
            return null;
        }
        return rs.getRightsStatementIdentifier().getRightsStatementIdentifierValue();
    }

    private static final Comparator<AgentIdentifier> AGENT_IDENTIFIER_COMPARATOR =
            Comparator.comparing(AgentIdentifier::getType, Comparator.comparing(Enum::name))
                    .thenComparing(AgentIdentifier::getValue);

    private static List<AgentIdentifier> normalizeIdentifiers(List<AgentIdentifier> identifiers) {
        if (identifiers == null || identifiers.isEmpty()) {
            return List.of();
        }
        return identifiers.stream()
                .filter(Objects::nonNull)
                .filter(i -> i.getType() != null)
                .filter(i -> i.getValue() != null && !i.getValue().isBlank())
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf));
    }

    private static List<AgentIdentifier> normalizeAgentIdentifiersFromPremis(AgentComplexType agent) {
        if (agent == null || agent.getAgentIdentifier() == null) {
            return List.of();
        }
        var result = new ArrayList<AgentIdentifier>();
        for (var i : agent.getAgentIdentifier()) {
            if (i == null || i.getAgentIdentifierType() == null) {
                continue;
            }
            String type = i.getAgentIdentifierType().getValue();
            String value = i.getAgentIdentifierValue();
            if (type == null || type.isBlank() || value == null || value.isBlank()) {
                continue;
            }
            try {
                var mappedType = AgentIdentifierType.valueOf(type.toUpperCase());
                if (mappedType == AgentIdentifierType.SYSTEM) {
                    continue;
                }
                result.add(new AgentIdentifier(mappedType, value));
            } catch (RuntimeException ex) {
                result.add(new AgentIdentifier(AgentIdentifierType.OTHER, value));
            }
        }
        return normalizeIdentifiers(result);
    }

    static boolean agentMatchesExisting(AgentComplexType existing, AgentMetadata desired) {
        if (existing == null || desired == null) {
            return false;
        }

        if (desired.getId() != null && !desired.getId().isBlank()) {
            return Objects.equals(toXmlId(desired.getId()), existing.getXmlID());
        }

        var desiredIdentifiers = normalizeIdentifiers(desired.getIdentifiers()).stream()
                .filter(i -> i.getType() != AgentIdentifierType.SYSTEM)
                .toList();
        if (desiredIdentifiers.isEmpty()) {
            return false;
        }

        var existingIdentifiers = normalizeAgentIdentifiersFromPremis(existing);
        if (existingIdentifiers.isEmpty()) {
            return false;
        }

        return new LinkedHashSet<>(desiredIdentifiers).equals(new LinkedHashSet<>(existingIdentifiers));
    }

    private static String toXmlId(String id) {
        return "id_" + id;
    }

    private static String resolveStableAgentId(AgentMetadata agent) {
        var normalized = normalizeIdentifiers(agent == null ? null : agent.getIdentifiers());
        if (normalized.isEmpty()) {
            return null;
        }
        var seed = normalized.stream()
                .sorted(AGENT_IDENTIFIER_COMPARATOR)
                .map(i -> i.getType().name() + ":" + i.getValue())
                .collect(Collectors.joining("|"));
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
    }

    public void upsertRightsStatement(PremisComplexType premis,
                                      UUID objectId,
                                      RightsStatementMetadata rightsStatement,
                                      List<AgentMetadata> agentsToEnsure) {
        if (premis == null) {
            throw new IllegalArgumentException("premis must not be null");
        }
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (rightsStatement == null) {
            throw new IllegalArgumentException("rightsStatement must not be null");
        }

        if (agentsToEnsure != null) {
            for (var agent : agentsToEnsure) {
                ensureAgentPresent(premis, agent);
            }
        }

        String rightsStatementId = rightsStatement.getId() == null || rightsStatement.getId().isBlank()
                ? UUID.randomUUID().toString()
                : rightsStatement.getId();

        if (!Objects.equals(rightsStatementId, rightsStatement.getId())) {
            rightsStatement.setId(rightsStatementId);
        }

        RightsComplexType rights = findOrCreateRightsContainer(premis, rightsStatementId);

        RightsStatementComplexType rightsStatementXml = upsertRightsStatement(rights, objectId, rightsStatement);

        var items = rights.getRightsStatementOrRightsExtension();
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (!(item instanceof RightsStatementComplexType existing)) {
                continue;
            }
            var existingId = extractRightsStatementIdentifierValue(existing);
            if (Objects.equals(existingId, rightsStatementId)) {
                items.set(i, rightsStatementXml);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            items.add(rightsStatementXml);
        }

        addRightsStatementUpdateEvent(premis, objectId, rightsStatementId);
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

    private RightsStatementComplexType upsertRightsStatement(RightsComplexType rights,
                                                            UUID objectId,
                                                            RightsStatementMetadata desired) {
        RightsStatementComplexType existing = findExistingRightsStatement(rights, desired.getId());

        RightsStatementMetadata model = existing != null
                ? premisRightsStatementConverter.map(existing)
                : RightsStatementMetadata.builder().build();

        model.setId(desired.getId());
        model.setRightsBasis(desired.getRightsBasis());
        model.setCopyrightInformation(desired.getCopyrightInformation());
        model.setLicenseInformation(desired.getLicenseInformation());
        model.setStatuteInformation(desired.getStatuteInformation());
        model.setOtherRightsInformation(desired.getOtherRightsInformation());
        model.setRightsGranted(desired.getRightsGranted());
        model.setLinkingAgentIdentifiers(desired.getLinkingAgentIdentifiers());

        var rs = rightsStatementConverter.map(model);

        rs.getLinkingObjectIdentifier().clear();
        rs.getLinkingObjectIdentifier().add(buildLinkingObjectIdentifier(objectId));

        rs.getLinkingAgentIdentifier().clear();
        if (desired.getLinkingAgentIdentifiers() != null) {
            for (RightsStatementAgentLink link : desired.getLinkingAgentIdentifiers()) {
                if (link == null || link.getAgentIdentifier() == null
                        || link.getAgentIdentifier().getType() == null
                        || link.getAgentIdentifier().getValue() == null
                        || link.getAgentIdentifier().getValue().isBlank()) {
                    continue;
                }
                rs.getLinkingAgentIdentifier().add(buildLinkingAgentIdentifier(
                        link.getAgentIdentifier().getType().name(),
                        link.getAgentIdentifier().getValue(),
                        link.getRoles() == null ? List.of() : List.copyOf(link.getRoles())));
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

    private LinkingAgentIdentifierComplexType buildLinkingAgentIdentifier(String agentIdentifierType,
                                                                          String agentIdentifierValue,
                                                                          List<String> roles) {
        var link = new LinkingAgentIdentifierComplexType();
        link.setLinkingAgentIdentifierType(toStringPlusAuthority(agentIdentifierType));
        link.setLinkingAgentIdentifierValue(agentIdentifierValue);
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
        if (premis == null || agent == null) {
            return;
        }

        if (premis.getAgent() != null && premis.getAgent().stream()
                .filter(Objects::nonNull)
                .anyMatch(existing -> agentMatchesExisting(existing, agent))) {
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
