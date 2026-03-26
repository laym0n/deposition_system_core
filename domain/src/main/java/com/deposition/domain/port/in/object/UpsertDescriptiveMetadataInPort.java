package com.deposition.domain.port.in.object;

import com.deposition.domain.port.in.schema.IntellectualEntityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.UUID;

@Validated
public interface UpsertDescriptiveMetadataInPort {

    Map<String, Object> upsertDescriptiveMetadata(
            @NotNull UUID objectId,
            @NotNull @Valid IntellectualEntityType entityType,
            @NotBlank String descriptiveMetadataJson);
}
