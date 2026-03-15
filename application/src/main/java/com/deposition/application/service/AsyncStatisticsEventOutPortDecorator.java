package com.deposition.application.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.deposition.domain.models.statistics.StatisticsEvent;
import com.deposition.domain.port.out.StatisticsEventOutPort;

import lombok.extern.slf4j.Slf4j;

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
