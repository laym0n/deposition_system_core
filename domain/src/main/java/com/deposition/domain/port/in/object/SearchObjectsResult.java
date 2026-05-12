package com.deposition.domain.port.in.object;

import jakarta.validation.constraints.NotNull;
import com.deposition.domain.models.IntellectualEntityType;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.UUID;

public record SearchObjectsResult(
        long total,
        @NotNull
        List<Hit> hits) {

    public record Hit(
            @NotNull
            UUID objectId,
            @Nullable
            IntellectualEntityType intellectualEntityType,
            String originalName) {

    }
}
