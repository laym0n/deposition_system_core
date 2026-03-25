package com.deposition.domain.port.in.schema;

import com.deposition.domain.models.DescriptiveMetadataSchema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface UpdateDescriptiveMetadataSchemaActiveInPort {

    DescriptiveMetadataSchema updateActive(@NotNull UUID schemaId, @NotNull @Valid UpdateActiveCommand command);

    record UpdateActiveCommand(@NotNull Boolean active) {

    }
}
