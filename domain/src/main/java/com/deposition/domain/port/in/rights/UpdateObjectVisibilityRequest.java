package com.deposition.domain.port.in.rights;

import jakarta.validation.constraints.NotNull;

public record UpdateObjectVisibilityRequest(
        @NotNull Visibility visibility) {

    public enum Visibility {
        PUBLIC,
        PRIVATE
    }
}
