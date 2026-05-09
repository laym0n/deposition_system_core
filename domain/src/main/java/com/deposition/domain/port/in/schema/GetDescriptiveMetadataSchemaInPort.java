package com.deposition.domain.port.in.schema;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public interface GetDescriptiveMetadataSchemaInPort {

    Map<String, Object> getSchema(@NotBlank String entityTypeName);
}
