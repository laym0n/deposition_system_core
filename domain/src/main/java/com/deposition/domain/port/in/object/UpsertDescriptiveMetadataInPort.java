package com.deposition.domain.port.in.object;

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
            @NotBlank String entityTypeName,
            @NotBlank String descriptiveMetadataJson);
}
