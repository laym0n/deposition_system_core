package com.deposition.domain.models.statistics;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.annotation.Nullable;

public record StatisticsEvent(
        UUID id,
        StatisticsEventType eventType,
        @Nullable
        UUID objectId,
        @Nullable
        String objectVersion,
        String userId,
        OffsetDateTime timestamp) {

}
