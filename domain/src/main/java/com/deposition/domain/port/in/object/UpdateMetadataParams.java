package com.deposition.domain.port.in.object;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

import java.util.List;

public record UpdateMetadataParams(
        @Nullable
        @Valid
        IntellectualEntityMetadataParam intellectualEntityMetadata,
        @Nullable
        List<@Valid UpdateRepresentationMetadataParam> representations) {

}
