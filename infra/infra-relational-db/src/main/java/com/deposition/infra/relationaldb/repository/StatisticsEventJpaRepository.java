package com.deposition.infra.relationaldb.repository;

import com.deposition.infra.relationaldb.entity.StatisticsEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface StatisticsEventJpaRepository extends JpaRepository<StatisticsEventEntity, UUID> {

    @Query("""
            select e
            from StatisticsEventEntity e
            where e.objectId = :objectId
              and e.timestamp >= :from
              and e.timestamp <= :to
              and (:eventType is null or e.eventType = :eventType)
            order by e.timestamp asc
            """)
    List<StatisticsEventEntity> findByObjectIdAndTimestampBetween(
            @Param("objectId") UUID objectId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("eventType") String eventType);
}
