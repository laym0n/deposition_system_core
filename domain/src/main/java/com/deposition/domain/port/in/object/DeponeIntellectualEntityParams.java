package com.deposition.domain.port.in.object;

import java.util.List;

import com.deposition.domain.port.in.schema.IntellectualEntityType;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DeponeIntellectualEntityParams(
        @NotNull
        IntellectualEntityType intellectualEntityType,
        @Nullable
        @Valid
        IntellectualEntityMetadataParam intellectualEntityMetadata,
        @Nullable
        String descriptiveMetadata,
        @NotEmpty
        List<@Valid DeponeRepresentationParam> representations) {

}
