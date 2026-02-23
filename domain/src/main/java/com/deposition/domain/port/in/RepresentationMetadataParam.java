package com.deposition.domain.port.in;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

public record RepresentationMetadataParam(@NotBlank @Nullable String originalName) {

}
