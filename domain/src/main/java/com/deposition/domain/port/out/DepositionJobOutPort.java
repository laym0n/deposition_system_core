package com.deposition.domain.port.out;

import com.deposition.domain.models.depositionjob.DepositionJob;
import com.deposition.domain.models.depositionjob.DepositionJobFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepositionJobOutPort {

    DepositionJob create(@NotNull @Valid DepositionJob job);

    DepositionJob update(@NotNull @Valid DepositionJob job);

    Optional<DepositionJob> findById(@NotNull UUID jobId);

    Optional<DepositionJob> findByOwnerAndIdempotencyKey(@NotBlank String ownerUserId, @NotBlank String idempotencyKey);

    DepositionJobPage listByOwnerUserId(@NotBlank String ownerUserId, int page, int size);

    record DepositionJobPage(
            @NotNull List<@NotNull DepositionJob> items,
            long totalItems
    ) {
    }

    List<DepositionJobFile> listFiles(@NotNull UUID jobId);

    void upsertFiles(@NotNull UUID jobId, @NotNull List<@Valid DepositionJobFile> files);
}
