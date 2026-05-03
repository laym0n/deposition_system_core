package com.deposition.domain.port.in.statistics;

import com.deposition.domain.models.statistics.StatisticsEventType;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface GetStatisticsEventsInPort {

    List<StatisticsEventItem> getEvents(@NotNull @Valid GetStatisticsEventsRequest request);

    record GetStatisticsEventsRequest(
            @NotNull UUID objectId,
            @NotNull OffsetDateTime from,
            @NotNull OffsetDateTime to,
            @Nullable StatisticsEventType eventType) {
    }

    record StatisticsEventItem(
            UUID id,
            StatisticsEventType eventType,
            UUID objectId,
            String objectVersion,
            String userId,
            OffsetDateTime timestamp) {
    }
}
