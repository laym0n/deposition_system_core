package com.deposition.domain.port.in.schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public interface GetDescriptiveMetadataSchemaInPort {

    Map<String, Object> getSchema(@NotNull @Valid IntellectualEntityType entityType);
}
