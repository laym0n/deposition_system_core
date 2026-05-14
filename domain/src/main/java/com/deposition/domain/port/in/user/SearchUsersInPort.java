package com.deposition.domain.port.in.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface SearchUsersInPort {

    List<UserSummary> search(@NotNull @Valid SearchUsersRequest request);

    record UserSummary(
            @NotNull String id,
            @NotNull String username) {
    }
}
