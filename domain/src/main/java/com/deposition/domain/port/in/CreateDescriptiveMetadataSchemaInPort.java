package com.deposition.domain.port.in;

import com.deposition.domain.models.DescriptiveMetadataSchema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface CreateDescriptiveMetadataSchemaInPort {

    DescriptiveMetadataSchema create(@NotNull @Valid CreateDescriptiveMetadataSchemaCommand command);

    record CreateDescriptiveMetadataSchemaCommand(
            @NotNull IntellectualEntityType entityType,
            @NotNull String schemaJson) {

    }
}
