package com.deposition.domain.port.in;

import java.util.List;

import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.models.valueobject.Relationship;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record IntellectualEntityMetadataParam(
                @NotBlank @Nullable String originalName,
                @Nullable List<@Valid ObjectIdentifier> identifiers,
                @Nullable List<@Valid Relationship> relationships) {

}
