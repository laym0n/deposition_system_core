package com.deposition.domain.port.out;

import com.deposition.domain.port.in.object.ObjectSearchRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ObjectSearchQuery(
        @NotNull
        @Valid
        ObjectSearchRequest request,
        @NotNull
        @Valid
        ObjectSearchFilters filters) {
}
