package com.deposition.application.service;

import com.deposition.domain.models.statistics.StatisticsEvent;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.out.StatisticsEventOutPort;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Primary
@Slf4j
public class AsyncStatisticsEventOutPortDecorator implements StatisticsEventOutPort {

    private final StatisticsEventOutPort delegate;

    public AsyncStatisticsEventOutPortDecorator(
            @Qualifier("jpaStatisticsEventAdapter") StatisticsEventOutPort delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(StatisticsEvent event) {
        saveAsync(event);
    }

    @Override
    public List<StatisticsEvent> findByObjectIdAndTimestampBetween(
            UUID objectId,
            OffsetDateTime from,
            OffsetDateTime to,
            @Nullable StatisticsEventType eventType) {
        return delegate.findByObjectIdAndTimestampBetween(objectId, from, to, eventType);
    }

    @Async("statisticsEventExecutor")
    void saveAsync(StatisticsEvent event) {
        try {
            delegate.save(event);
        } catch (Exception ex) {
            log.warn("Failed to write StatisticsEvent (async): type={}, objectId={}, userId={}",
                    event == null ? null : event.eventType(),
                    event == null ? null : event.objectId(),
                    event == null ? null : event.userId(),
                    ex);
        }
    }
}
