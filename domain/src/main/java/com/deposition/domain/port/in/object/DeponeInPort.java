package com.deposition.domain.port.in.object;

import com.deposition.domain.port.in.common.DepositionResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface DeponeInPort {

    DepositionResult depone(@NotNull @Valid DeponeIntellectualEntityParams params);
}
