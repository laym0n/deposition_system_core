package com.deposition.domain.port.in.event;

import com.deposition.domain.port.in.common.DepositionResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface RecordObjectEventInPort {

    DepositionResult recordEvent(@NotNull UUID objectId, @NotNull @Valid RecordObjectEventRequest request);
}
