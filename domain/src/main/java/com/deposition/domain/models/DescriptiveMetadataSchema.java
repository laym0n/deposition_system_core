package com.deposition.domain.models;

import com.deposition.domain.port.in.schema.IntellectualEntityType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DescriptiveMetadataSchema(
        UUID id,
        IntellectualEntityType entityType,
        String schemaJson,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

}
