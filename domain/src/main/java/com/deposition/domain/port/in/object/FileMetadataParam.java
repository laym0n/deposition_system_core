package com.deposition.domain.port.in.object;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

public record FileMetadataParam(@NotBlank
                                @Nullable
                                String originalName) {

}
