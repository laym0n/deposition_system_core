package com.deposition.domain.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.deposition.domain.models.statistics.StatisticsEvent;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.out.BusinessMetricsOutPort;
import com.deposition.domain.port.out.StatisticsEventOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatisticsEventReporter {

    private final StatisticsEventOutPort outPort;
    private final Clock clock;
    private final BusinessMetricsOutPort businessMetrics;

    public void report(StatisticsEventType eventType,
            UUID objectId,
            String objectVersion,
            String userId) {

        if (businessMetrics != null && eventType != null) {
            switch (eventType) {
                case OBJECT_DEPOSIT ->
                    businessMetrics.incrementDepositionOperations();
                case OBJECT_METADATA_UPDATE ->
                    businessMetrics.incrementObjectVersionUpdates();
                case PROOF_REQUEST ->
                    businessMetrics.incrementProofRequests();
                case OBJECT_VIEW ->
                    businessMetrics.incrementObjectViews();
                default -> {
                }
            }
        }

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
