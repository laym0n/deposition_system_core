package com.deposition.domain.port.in.event;

import java.util.List;

import com.deposition.domain.models.enums.EventType;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
