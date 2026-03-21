package com.deposition.domain.port.in.object;

import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DeponeRepresentationParam(
        @Nullable
        @Valid
        RepresentationMetadataParam representationMetadata,
        @NotEmpty
        List<@NotNull @Valid DeponeFileParam> fileParams) {

}
