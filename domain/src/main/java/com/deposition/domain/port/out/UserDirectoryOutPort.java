package com.deposition.domain.port.out;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * External user directory (e.g. Keycloak).
 */
public interface UserDirectoryOutPort {

    List<UserSummary> search(@NotNull @Valid UserSearchQuery query);

    record UserSearchQuery(
            String searchQuery,
            int offset,
            int limit) {
    }

    record UserSummary(
            @NotNull String id,
            @NotNull String username) {
    }
}
