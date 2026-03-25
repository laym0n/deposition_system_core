package com.deposition.infra.relationaldb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "statistics_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsEventEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "object_id")
    private UUID objectId;

    @Column(name = "object_version")
    private String objectVersion;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @Column(name = "event_timestamp", nullable = false)
    private OffsetDateTime timestamp;
}
