package com.deposition.domain.port.in;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

public interface GetDescriptiveMetadataSchemasInPort {

    List<DescriptiveMetadataSchemaSummary> getSchemas(@Valid DescriptiveMetadataSchemaFilter filter);

    record DescriptiveMetadataSchemaFilter(
            IntellectualEntityType entityType,
            Boolean active) {

    }

    record DescriptiveMetadataSchemaSummary(
            UUID id,
            IntellectualEntityType entityType,
            boolean active,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {

    }
}
