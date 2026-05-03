package com.deposition.domain.port.out;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Generates presigned download URL for a stored file.
 *
 * <p>We sign by objectKey (storage-specific), but keep returning the stable contentLocation used across the system.
 */
public interface PresignDownloadOutPort {

    PresignedDownload presignGetObject(@NotNull @Valid PresignGetObjectCommand command);

    record PresignGetObjectCommand(
            @NotNull URI contentLocation,
            @NotNull Duration expiresIn) {
    }

    record PresignedDownload(
            @NotNull URI downloadUrl,
            @NotNull URI contentLocation,
            @NotNull OffsetDateTime expiresAt) {
    }
}
