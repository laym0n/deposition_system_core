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
            /** UI card title / object name. */
            String objectName,
            /** Intellectual entity type of the deposited object. */
            String intellectualEntityTypeName,
            @NotNull DepositionJobStatus status,
            @NotNull OffsetDateTime createdAt,
            @NotNull OffsetDateTime updatedAt
    ) {
    }
}
