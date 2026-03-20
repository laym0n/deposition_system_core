package com.deposition.domain.port.in;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface GetDescriptiveMetadataSchemaInPort {

    Map<String, Object> getSchema(@NotNull @Valid IntellectualEntityType entityType);
}
