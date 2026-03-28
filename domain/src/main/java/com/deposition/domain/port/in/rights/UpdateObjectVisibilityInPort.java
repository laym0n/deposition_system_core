package com.deposition.domain.port.in.rights;

import com.deposition.domain.port.in.common.DepositionResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface UpdateObjectVisibilityInPort {

    DepositionResult updateVisibility(@NotNull UUID objectId,
                                     @NotNull @Valid UpdateObjectVisibilityRequest request);
}
