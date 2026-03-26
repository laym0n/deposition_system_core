package com.deposition.domain.port.in.event;

import com.deposition.domain.models.enums.EventType;
import com.deposition.domain.models.valueobject.EventDetailInformation;
import com.deposition.domain.models.valueobject.EventOutcomeInformation;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RecordObjectEventRequest(
        @NotNull
        EventType type,
        @Nullable
        List<EventDetailInformation> detail,
        @Nullable
        List<EventOutcomeInformation> outcome) {

}
