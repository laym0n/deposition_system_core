package com.deposition.domain.port.in;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface GetDescriptiveMetadataSchemaByIdInPort {

    Map<String, Object> getSchema(@NotNull @Valid UUID schemaId);
}
