package com.deposition.domain.port.in;

import java.util.List;
import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateRepresentationMetadataParam(
        @NotNull
        UUID representationId,
        @Nullable
        @Valid
        RepresentationMetadataParam representationMetadata,
        @Nullable
        List<@Valid UpdateFileMetadataParam> files) {

}
