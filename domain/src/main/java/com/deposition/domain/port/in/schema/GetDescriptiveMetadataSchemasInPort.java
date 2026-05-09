package com.deposition.domain.port.in.schema;

import jakarta.validation.Valid;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface GetDescriptiveMetadataSchemasInPort {

    List<DescriptiveMetadataSchemaSummary> getSchemas(@Valid DescriptiveMetadataSchemaFilter filter);

    record DescriptiveMetadataSchemaFilter(
            String entityTypeName,
            Boolean active) {

    }

    record DescriptiveMetadataSchemaSummary(
            UUID id,
            String entityTypeName,
            boolean active,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {

    }
}
