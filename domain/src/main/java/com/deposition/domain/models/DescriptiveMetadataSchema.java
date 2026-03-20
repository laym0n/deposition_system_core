package com.deposition.domain.models;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.deposition.domain.port.in.IntellectualEntityType;

public record DescriptiveMetadataSchema(
        UUID id,
        IntellectualEntityType entityType,
        String schemaJson,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

}
