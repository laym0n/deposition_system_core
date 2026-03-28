package com.deposition.domain.port.in.object;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ObjectSearchRequest(
        @Nullable
        String searchQuery,
        @Min(0)
        int offset,
        @Nullable
        @Min(1)
        @Max(500)
        Integer limit) {

    public int effectiveLimit() {
        return limit == null ? 50 : limit;
    }
}
