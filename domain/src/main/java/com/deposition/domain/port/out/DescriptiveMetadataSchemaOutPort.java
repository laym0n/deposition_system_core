package com.deposition.domain.port.out;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.deposition.domain.models.DescriptiveMetadataSchema;

import jakarta.validation.constraints.NotBlank;

public interface DescriptiveMetadataSchemaOutPort {

    Optional<String> findActiveSchemaJsonByEntityType(@NotBlank String entityType);

    Optional<DescriptiveMetadataSchema> findById(UUID id);

    DescriptiveMetadataSchema save(DescriptiveMetadataSchema schema);

    List<DescriptiveMetadataSchemaSummary> findSchemas(DescriptiveMetadataSchemaFilter filter);

    record DescriptiveMetadataSchemaFilter(
            String entityType,
            Boolean active) {

    }

    record DescriptiveMetadataSchemaSummary(
            UUID id,
            String entityType,
            boolean active,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {

    }
}
