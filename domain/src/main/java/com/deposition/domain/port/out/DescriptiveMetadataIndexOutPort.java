package com.deposition.domain.port.out;

import com.deposition.domain.port.in.schema.IntellectualEntityType;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public interface DescriptiveMetadataIndexOutPort {

    void index(@NotNull UUID intellectualEntityId,
               @NotNull IntellectualEntityType entityType,
               @NotNull Map<String, Object> extractedFields);
}
