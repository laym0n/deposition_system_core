package com.deposition.domain.port.in;

import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record DeponeIntellectualEntityParams(
                @Nullable @Valid IntellectualEntityMetadataParam intellectualEntityMetadata,
                @NotEmpty List<@Valid DeponeRepresentationParam> representations) {

}
