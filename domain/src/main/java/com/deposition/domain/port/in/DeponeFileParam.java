package com.deposition.domain.port.in;

import org.springframework.core.io.Resource;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DeponeFileParam(
        @Nullable @Valid FileMetadataParam fileMetadata,
        @NotNull Resource resource) {

}
