package com.deposition.domain.port.in.object;

import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateFileMetadataParam(
        @NotNull
        UUID fileId,
        @Nullable
        @Valid
        FileMetadataParam fileMetadata) {

}
