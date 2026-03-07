package com.deposition.domain.port.in;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SearchObjectsResult(
        long total,
        @NotNull
        List<Hit> hits) {

    public record Hit(
            @NotNull
            UUID objectId,
            @NotNull
            String entityType) {

    }
}
