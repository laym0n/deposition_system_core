package com.deposition.domain.port.in.depositionjob;

import com.deposition.domain.port.in.common.DepositionResult;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface GetDepositionJobStatusInPort {

    DepositionJobStatusResponse getStatus(@NotNull UUID jobId);

    record DepositionJobStatusResponse(
            @NotNull
            UUID jobId,
            @NotNull
            UUID objectId,
            @NotNull
            DepositionJobStatus status,
            @NotNull
            OffsetDateTime createdAt,
            @NotNull
            OffsetDateTime updatedAt,
            @Nullable
            DepositionResult result,
            @Nullable
            String errorMessage) {
    }
}
