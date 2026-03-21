package com.deposition.domain.port.in.object;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Validated
public interface DeponeInPort {

    DeponeResult depone(@NotNull @Valid DeponeIntellectualEntityParams params);
}
