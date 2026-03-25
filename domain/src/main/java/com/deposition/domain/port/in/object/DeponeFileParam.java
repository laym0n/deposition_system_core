package com.deposition.domain.port.in.object;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;

public record DeponeFileParam(
        @Nullable
        @Valid
        FileMetadataParam fileMetadata,
        @NotNull
        Resource resource) {

}
