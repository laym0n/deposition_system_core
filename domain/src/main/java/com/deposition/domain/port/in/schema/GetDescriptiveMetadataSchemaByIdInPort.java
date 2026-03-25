package com.deposition.domain.port.in.schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public interface GetDescriptiveMetadataSchemaByIdInPort {

    Map<String, Object> getSchema(@NotNull @Valid UUID schemaId);
}
