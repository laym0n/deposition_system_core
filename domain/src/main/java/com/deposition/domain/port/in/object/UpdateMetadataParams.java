package com.deposition.domain.port.in.object;

import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;

public record UpdateMetadataParams(
        @Nullable
        @Valid
        IntellectualEntityMetadataParam intellectualEntityMetadata,
        @Nullable
        List<@Valid UpdateRepresentationMetadataParam> representations) {

}
