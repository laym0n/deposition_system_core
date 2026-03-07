package com.deposition.domain.port.out;

import com.deposition.domain.port.in.ObjectSearchRequest;
import com.deposition.domain.port.in.SearchObjectsResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface ObjectSearchOutPort {

    SearchObjectsResult search(@NotNull String userId, @NotNull @Valid ObjectSearchRequest request);
}
