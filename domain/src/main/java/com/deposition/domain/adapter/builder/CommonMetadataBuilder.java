package com.deposition.domain.adapter.builder;

import com.deposition.domain.dto.schema.premis.v3.ObjectComplexType;
import com.deposition.domain.models.valueobject.Storage;
import com.deposition.domain.port.in.object.DeponeFileParam;
import com.deposition.domain.port.in.object.RepresentationMetadataParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public final class CommonMetadataBuilder {

    MetadataStructure toMetadataStructure(UUID objectId, ObjectComplexType objectMetadata) {
        return new MetadataStructure(objectId, objectMetadata);
    }

    record MetadataStructure(
            UUID objectId,
            ObjectComplexType objectMetadata) {

    }

    public record PersistedRepresentationMetadataInput(
            RepresentationMetadataParam representationMetadata,
            List<PersistedFileMetadataInput> persistedFiles) {

    }

    public record PersistedFileMetadataInput(
            DeponeFileParam fileParam,
            Storage fileStorage,
            String hashAlgorithm,
            String hashHex,
            long sizeBytes) {

    }
}
