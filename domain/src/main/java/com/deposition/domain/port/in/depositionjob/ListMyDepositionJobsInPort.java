package com.deposition.domain.port.in.depositionjob;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Lists deposition jobs of current authenticated user.
 */
public interface ListMyDepositionJobsInPort {

    @NotNull
    List<DepositionJobListItem> listMyJobs();

    record DepositionJobListItem(
            @NotNull UUID jobId,
            @NotNull UUID objectId,
            @NotNull DepositionJobStatus status,
            @NotNull OffsetDateTime createdAt,
            @NotNull OffsetDateTime updatedAt
    ) {
    }
}
