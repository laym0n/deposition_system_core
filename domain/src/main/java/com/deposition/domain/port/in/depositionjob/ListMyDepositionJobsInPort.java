package com.deposition.domain.port.in.depositionjob;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
public interface ListMyDepositionJobsInPort {

    @NotNull
    DepositionJobPage listMyJobs(@NotNull ListMyJobsQuery query);

    record ListMyJobsQuery(
            int page,
            int size
    ) {
        public ListMyJobsQuery {
            if (page < 0) {
                throw new IllegalArgumentException("page must be >= 0");
            }
            if (size <= 0) {
                throw new IllegalArgumentException("size must be > 0");
            }
        }
    }

    record DepositionJobPage(
            @NotNull List<@NotNull DepositionJobListItem> items,
            int page,
            int size,
            long totalItems
    ) {
    }

    record DepositionJobListItem(
            @NotNull UUID jobId,
            @NotNull UUID objectId,
            String objectName,
            String intellectualEntityTypeName,
            @NotNull DepositionJobStatus status,
            @NotNull OffsetDateTime createdAt,
            @NotNull OffsetDateTime updatedAt
    ) {
    }
}
