package com.deposition.domain.adapter.statistics;

import com.deposition.domain.port.in.statistics.GetStatisticsEventsInPort;
import com.deposition.domain.port.out.StatisticsEventOutPort;
import com.deposition.domain.service.acl.AccessValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@Validated
@RequiredArgsConstructor
public class GetStatisticsEventsAdapter implements GetStatisticsEventsInPort {

    private final AccessValidatorService accessValidatorService;
    private final StatisticsEventOutPort statisticsEventOutPort;

    @Override
    public List<StatisticsEventItem> getEvents(GetStatisticsEventsRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.objectId() == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (request.from() == null) {
            throw new IllegalArgumentException("from must not be null");
        }
        if (request.to() == null) {
            throw new IllegalArgumentException("to must not be null");
        }
        if (request.from().isAfter(request.to())) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }

        accessValidatorService.validateCurrentUserCanRead(request.objectId());

        var events = statisticsEventOutPort.findByObjectIdAndTimestampBetween(
                request.objectId(),
                request.from(),
                request.to(),
                request.eventType());

        return events.stream()
                .map(e -> new StatisticsEventItem(
                        e.id(),
                        e.eventType(),
                        e.objectId(),
                        e.objectVersion(),
                        e.userId(),
                        e.timestamp()))
                .toList();
    }
}
