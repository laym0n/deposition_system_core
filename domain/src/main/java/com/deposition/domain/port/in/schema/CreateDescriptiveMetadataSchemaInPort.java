package com.deposition.domain.port.in.schema;

import com.deposition.domain.models.DescriptiveMetadataSchema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface CreateDescriptiveMetadataSchemaInPort {

    DescriptiveMetadataSchema create(@NotNull @Valid CreateDescriptiveMetadataSchemaCommand command);

    record CreateDescriptiveMetadataSchemaCommand(
            @NotBlank String entityTypeName,
            @NotNull String schemaJson) {

    }
}
