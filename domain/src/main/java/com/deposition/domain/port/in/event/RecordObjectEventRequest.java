package com.deposition.domain.port.in.event;

import com.deposition.domain.models.enums.EventType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecordObjectEventRequest(
        @NotNull
        EventType type,
        @NotBlank
        String detail,
        @Nullable
        List<String> outcome,
        @Nullable
        List<String> outcomeDetail) {

}
