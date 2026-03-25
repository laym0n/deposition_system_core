package com.deposition.domain.models.statistics;

import jakarta.annotation.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

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
