package com.deposition.domain.port.in.object;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record UpdateRepresentationMetadataParam(
        @NotNull
        UUID representationId,
        @Nullable
        @Valid
        RepresentationMetadataParam representationMetadata,
        @Nullable
        List<@Valid UpdateFileMetadataParam> files) {

}
