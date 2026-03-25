package com.deposition.domain.adapter.builder;

import com.deposition.domain.dto.schema.premis.v3.EventComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.EventConverter;
import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.enums.*;
import com.deposition.domain.models.valueobject.*;
import com.deposition.domain.port.in.object.DeponeFileParam;
import com.deposition.domain.port.in.object.RepresentationMetadataParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public final class CommonMetadataBuilder {

    private final EventConverter eventConverter;

    private static List<EventAgentLink> buildAgentLinks(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        var agentIdentifier = new AgentIdentifier(AgentIdentifierType.SYSTEM, authentication.getName());
        return List.of(new EventAgentLink(agentIdentifier, List.of(EventAgentLinkRole.AUTHORIZER)));
    }

    MetadataStructure toMetadataStructure(UUID objectId, ObjectComplexType objectMetadata, Authentication authentication) {
        var eventId = UUID.randomUUID();
        var creationEvent = buildCreationEvent(eventId, objectId, authentication);
        return new MetadataStructure(objectId, objectMetadata, creationEvent);
    }

    private EventComplexType buildCreationEvent(UUID eventId, UUID objectId, Authentication authentication) {
        var event = EventMetadata.builder()
                .id(eventId)
                .type(EventType.CREATION)
                .dateTime(OffsetDateTime.now())
                .objectLinks(List.of(new EventObjectLink(
                        new ObjectIdentifier(
                                ObjectIdentifierType.SYSTEM, objectId.toString()),
                        List.of(EventObjectLinkRole.OUTCOME))))
                .agentLinks(buildAgentLinks(authentication))
                .build();
        return eventConverter.map(event);
    }

    record MetadataStructure(
            UUID objectId,
            ObjectComplexType objectMetadata,
            EventComplexType creationEvent) {

    }

    public record PersistedRepresentationMetadataInput(
            RepresentationMetadataParam representationMetadata,
            List<PersistedFileMetadataInput> persistedFiles) {

    }

    public record PersistedFileMetadataInput(
            DeponeFileParam fileParam,
            Storage fileStorage) {

    }
}
