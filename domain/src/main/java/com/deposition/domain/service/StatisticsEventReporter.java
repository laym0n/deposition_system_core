package com.deposition.domain.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.deposition.domain.models.statistics.StatisticsEvent;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.out.StatisticsEventOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatisticsEventReporter {

    private final StatisticsEventOutPort outPort;
    private final Clock clock;

    public void report(StatisticsEventType eventType,
            UUID objectId,
            String objectVersion,
            String userId) {

        var event = new StatisticsEvent(
                UUID.randomUUID(),
                eventType,
                objectId,
                objectVersion,
                userId,
                OffsetDateTime.now(clock));

        outPort.save(event);
    }
}
