package com.deposition.domain.models.depositionjob;

import com.deposition.domain.port.in.depositionjob.DepositionJobStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Deposition job aggregate root.
 * <p>
 * Previously this model existed as a nested record inside {@code DepositionJobOutPort}.
 */
public record DepositionJob(
        @NotNull UUID jobId,
        @NotNull UUID objectId,
        @NotBlank String ownerUserId,
        @NotNull DepositionJobStatus status,
        /** Persisted request JSON used later by processing pipeline. */
        @NotNull String requestJson,
        @Nullable String idempotencyKey,
        @NotNull OffsetDateTime createdAt,
        @NotNull OffsetDateTime updatedAt,
        @Nullable String resultTxId,
        @Nullable String resultVersionId,
        @Nullable String errorMessage) {
}
