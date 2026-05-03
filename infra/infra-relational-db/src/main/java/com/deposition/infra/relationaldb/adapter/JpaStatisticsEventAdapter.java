package com.deposition.infra.relationaldb.adapter;

import com.deposition.domain.models.statistics.StatisticsEvent;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.out.StatisticsEventOutPort;
import com.deposition.infra.relationaldb.mapper.StatisticsEventMapper;
import com.deposition.infra.relationaldb.repository.StatisticsEventJpaRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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

    @Override
    @Transactional(readOnly = true)
    public List<StatisticsEvent> findByObjectIdAndTimestampBetween(
            UUID objectId,
            OffsetDateTime from,
            OffsetDateTime to,
            @Nullable StatisticsEventType eventType) {

        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (from == null) {
            throw new IllegalArgumentException("from must not be null");
        }
        if (to == null) {
            throw new IllegalArgumentException("to must not be null");
        }

        var entities = repository.findByObjectIdAndTimestampBetween(
                objectId,
                from,
                to,
                eventType == null ? null : eventType.name());

        return entities.stream().map(mapper::toDomain).toList();
    }
}
