package com.deposition.domain.port.out;

import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.models.valueobject.Relationship;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PremisIndexFields(
        @NotNull
        UUID objectId,
        @Nullable
        String originalName,
        @Nullable
        List<ObjectIdentifier> identifiers,
        @Nullable
        List<Relationship> relationships) {
}
