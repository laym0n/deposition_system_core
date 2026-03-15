package com.deposition.infra.relationaldb.adapter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.deposition.domain.models.statistics.StatisticsEvent;
import com.deposition.domain.port.out.StatisticsEventOutPort;
import com.deposition.infra.relationaldb.mapper.StatisticsEventMapper;
import com.deposition.infra.relationaldb.repository.StatisticsEventJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@Qualifier("jpaStatisticsEventAdapter")
@RequiredArgsConstructor
public class JpaStatisticsEventAdapter implements StatisticsEventOutPort {

    private final StatisticsEventJpaRepository repository;
    private final StatisticsEventMapper mapper;

    @Override
    @Transactional
    public void save(StatisticsEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }

        repository.save(mapper.toEntity(event));
    }
}
