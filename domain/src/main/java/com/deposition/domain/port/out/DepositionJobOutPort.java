package com.deposition.domain.port.out;

import com.deposition.domain.port.in.depositionjob.DepositionJobStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepositionJobOutPort {

    DepositionJob create(@NotNull @Valid DepositionJob job);

    DepositionJob update(@NotNull @Valid DepositionJob job);

    Optional<DepositionJob> findById(@NotNull UUID jobId);

    Optional<DepositionJob> findByOwnerAndIdempotencyKey(@NotBlank String ownerUserId, @NotBlank String idempotencyKey);

    List<DepositionJobFile> listFiles(@NotNull UUID jobId);

    void upsertFiles(@NotNull UUID jobId, @NotNull List<@Valid DepositionJobFile> files);

    record DepositionJob(
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

    record DepositionJobFile(
            @NotNull UUID fileId,
            @NotNull UUID jobId,
            @NotNull String originalName,
            @Nullable String contentType,
            @Nullable Long sizeBytesExpected,
            @NotNull String objectKey,
            @NotNull URI contentLocation) {
    }
}
