package com.deposition.domain.port.out;

import com.deposition.domain.models.statistics.StatisticsEvent;
import com.deposition.domain.models.statistics.StatisticsEventType;

import jakarta.annotation.Nullable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface StatisticsEventOutPort {

    void save(StatisticsEvent event);

    List<StatisticsEvent> findByObjectIdAndTimestampBetween(
            UUID objectId,
            OffsetDateTime from,
            OffsetDateTime to,
            @Nullable StatisticsEventType eventType);
}
