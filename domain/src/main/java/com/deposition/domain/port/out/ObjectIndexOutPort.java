package com.deposition.domain.port.out;

import jakarta.validation.constraints.NotNull;

public interface ObjectIndexOutPort {

    void index(@NotNull ObjectIndexDocument document);
}
