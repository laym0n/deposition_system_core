package com.deposition.domain.adapter.builder;

import com.deposition.domain.dto.schema.premis.v3.AgentComplexType;
import com.deposition.domain.dto.schema.premis.v3.EventComplexType;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.AgentConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.EventConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisMetadataConverter;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.enums.*;
import com.deposition.domain.models.valueobject.*;
import com.deposition.domain.port.in.object.IntellectualEntityMetadataParam;
import com.deposition.domain.port.out.UserOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public final class PremisMetadataBuilder {

    private final FileMetadataBuilder fileMetadataBuilder;
    private final RepresentationMetadataBuilder representationMetadataBuilder;
    private final IntellectualEntityMetadataBuilder intellectualEntityMetadataBuilder;
    private final PremisMetadataConverter premisConverter;
    private final AgentConverter agentConverter;
    private final EventConverter eventConverter;
    private final UserOutPort userOutPort;

    private static AgentMetadata buildCurrentUserAgent(String userId) {
        return AgentMetadata.builder()
                .id(userId)
                .name(userId)
                .type(AgentType.PERSON)
                .identifiers(List.of(AgentIdentifier.builder()
                        .type(AgentIdentifierType.SYSTEM)
                        .value(userId)
                        .build()))
                .build();
    }

    private static List<EventAgentLink> buildEventAgentLinks(String userId) {
        var agentIdentifier = new AgentIdentifier(
                AgentIdentifierType.SYSTEM,
                userId);
        return List.of(new EventAgentLink(agentIdentifier, List.of(EventAgentLinkRole.AUTHORIZER)));
    }

    private EventComplexType buildCreationEventForObjects(UUID eventId,
                                                          List<UUID> objectIds,
                                                          String userId) {
        var links = objectIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .map(objectId -> new EventObjectLink(
                        new ObjectIdentifier(ObjectIdentifierType.SYSTEM, objectId.toString()),
                        List.of(EventObjectLinkRole.OUTCOME)))
                .toList();

        var event = EventMetadata.builder()
                .id(eventId)
                .identifier(new EventIdentifier(EventIdentifierType.SYSTEM, eventId.toString()))
                .type(EventType.CREATION)
                .dateTime(OffsetDateTime.now())
                .objectLinks(links)
                .agentLinks(buildEventAgentLinks(userId))
                .build();

        return eventConverter.map(event);
    }

    public PremisComplexType buildPremisWithEntities(
            List<CommonMetadataBuilder.PersistedRepresentationMetadataInput> persistedRepresentations,
            IntellectualEntityMetadataParam intellectualEntityMetadata, UUID intellectualEntityId) {
        var metadataStructures = new ArrayList<CommonMetadataBuilder.MetadataStructure>();
        var representationObjectIds = new ArrayList<UUID>();

        var userId = userOutPort.getCurrentUserId();

        for (var persistedRepresentation : persistedRepresentations) {
            var depositedFileObjectIds = new ArrayList<UUID>();
            for (var persistedFile : persistedRepresentation.persistedFiles()) {
                var fileMetadataStructure = fileMetadataBuilder.buildForFile(persistedFile);

                depositedFileObjectIds.add(fileMetadataStructure.objectId());
                metadataStructures.add(fileMetadataStructure);
            }

            var representationMetadataStructure = representationMetadataBuilder
                    .buildForRepresentation(depositedFileObjectIds, persistedRepresentation.representationMetadata());
            metadataStructures.add(representationMetadataStructure);
            representationObjectIds.add(representationMetadataStructure.objectId());
        }

        var intellectualEntityMetadataStructure = intellectualEntityMetadataBuilder.buildForIntellectualEntity(
                intellectualEntityMetadata, representationObjectIds, intellectualEntityId);
        metadataStructures.add(intellectualEntityMetadataStructure);

        var objectIds = metadataStructures.stream()
                .map(CommonMetadataBuilder.MetadataStructure::objectId)
                .toList();

        var eventId = UUID.randomUUID();
        var events = List.of(buildCreationEventForObjects(eventId, objectIds, userId));
        var objects = metadataStructures.stream().map(CommonMetadataBuilder.MetadataStructure::objectMetadata)
                .toList();

        var agent = buildCurrentUserAgent(userId);
        List<AgentComplexType> agents;
        if (agent == null) {
            agents = List.of();
        } else {
            agents = List.of(agentConverter.map(agent));
        }

        return premisConverter.map(objects, events, agents);
    }
}
