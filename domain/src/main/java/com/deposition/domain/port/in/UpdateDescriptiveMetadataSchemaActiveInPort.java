package com.deposition.domain.port.in;

import java.util.UUID;

import com.deposition.domain.models.DescriptiveMetadataSchema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface UpdateDescriptiveMetadataSchemaActiveInPort {

    DescriptiveMetadataSchema updateActive(@NotNull UUID schemaId, @NotNull @Valid UpdateActiveCommand command);

    record UpdateActiveCommand(@NotNull Boolean active) {

    }
}
