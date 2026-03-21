package com.deposition.domain.port.in.object;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface SearchObjectsInPort {

    SearchObjectsResult search(@NotNull @Valid ObjectSearchRequest request);
}
