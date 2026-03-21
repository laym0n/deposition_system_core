package com.deposition.domain.port.in.object;

import org.springframework.validation.annotation.Validated;

import com.deposition.domain.port.in.common.DepositionResult;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Validated
public interface DeponeInPort {

    DepositionResult depone(@NotNull @Valid DeponeIntellectualEntityParams params);
}
