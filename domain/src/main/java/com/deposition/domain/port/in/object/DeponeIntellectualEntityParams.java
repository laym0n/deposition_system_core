package com.deposition.domain.port.in.object;

import com.deposition.domain.port.in.schema.IntellectualEntityType;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

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
