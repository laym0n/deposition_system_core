package com.deposition.domain.models.depositionjob;

import com.deposition.domain.port.in.depositionjob.DepositionJobStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DepositionJob(
        @NotNull UUID jobId,
        @NotNull UUID objectId,
        @NotBlank String ownerUserId,
        @NotNull DepositionJobStatus status,
        @NotNull String requestJson,
        @Nullable String idempotencyKey,
        @NotNull OffsetDateTime createdAt,
        @NotNull OffsetDateTime updatedAt,
        @Nullable String resultTxId,
        @Nullable String resultVersionId,
        @Nullable String errorMessage) {
}
