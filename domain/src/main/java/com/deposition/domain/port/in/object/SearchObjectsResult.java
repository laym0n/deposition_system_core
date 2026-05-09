package com.deposition.domain.port.in.object;

import com.deposition.domain.models.IntellectualEntityType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SearchObjectsResult(
        long total,
        @NotNull
        List<Hit> hits) {

    public record Hit(
            @NotNull
            UUID objectId,
            @NotNull
            IntellectualEntityType intellectualEntityType,
            String originalName) {

    }
}
