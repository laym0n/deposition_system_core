package com.deposition.infra.relationaldb.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "deposition_job")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositionJobEntity {

    @Id
    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "object_id", nullable = false)
    private UUID objectId;

    @Column(name = "owner_user_id", nullable = false)
    private String ownerUserId;

    @Column(name = "status", nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "request_json", nullable = false)
    private String requestJson;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "result_tx_id")
    private String resultTxId;

    @Column(name = "result_version_id")
    private String resultVersionId;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "error_message")
    private String errorMessage;
}
