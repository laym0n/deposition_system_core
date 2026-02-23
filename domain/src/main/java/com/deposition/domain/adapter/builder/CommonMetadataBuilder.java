package com.deposition.domain.adapter.builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.deposition.domain.dto.schema.premis.v3.EventComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.EventConverter;
import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.enums.EventObjectLinkRole;
import com.deposition.domain.models.enums.EventType;
import com.deposition.domain.models.enums.ObjectIdentifierType;
import com.deposition.domain.models.valueobject.EventObjectLink;
import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.models.valueobject.Storage;
import com.deposition.domain.port.in.DeponeFileParam;
import com.deposition.domain.port.in.RepresentationMetadataParam;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class CommonMetadataBuilder {
    private final EventConverter eventConverter;

    MetadataStructure toMetadataStructure(UUID objectId, ObjectComplexType objectMetadata) {
        var eventId = UUID.randomUUID();
        var creationEvent = buildCreationEvent(eventId, objectId);
        return new MetadataStructure(objectId, objectMetadata, creationEvent);
    }

    private EventComplexType buildCreationEvent(UUID eventId, UUID objectId) {
        var event = EventMetadata.builder()
                .id(eventId)
                .type(EventType.CREATION)
                .dateTime(OffsetDateTime.now())
                .objectLinks(List.of(new EventObjectLink(
                        new ObjectIdentifier(
                                ObjectIdentifierType.LOCAL, objectId.toString()),
                        List.of(EventObjectLinkRole.OUTCOME))))
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
