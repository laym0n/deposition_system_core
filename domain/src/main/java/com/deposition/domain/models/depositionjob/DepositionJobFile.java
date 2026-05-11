package com.deposition.domain.models.depositionjob;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.util.UUID;

public record DepositionJobFile(
        @NotNull UUID fileId,
        @NotNull UUID jobId,
        @NotNull Integer representationIndex,
        @NotNull String originalName,
        @Nullable String contentType,
        @Nullable Long sizeBytesExpected,
        @NotNull String objectKey,
        @NotNull URI contentLocation) {
}
