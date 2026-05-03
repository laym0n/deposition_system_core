package com.deposition.infra.api.controller.object;

import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PresignedSourceFilesDownloadResponse(
        @NotNull UUID fileId,
        @NotNull URI downloadUrl,
        @NotNull OffsetDateTime expiresAt) {
}
