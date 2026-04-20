package com.deposition.domain.port.out;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

public interface PresignUploadOutPort {

    PresignedUpload presignPutObject(@NotNull @Valid PresignPutObjectCommand command);

    record PresignPutObjectCommand(
            @NotBlank String objectKey,
            @Nullable String contentType,
            @NotNull Duration expiresIn) {
    }

    record PresignedUpload(
            @NotNull URI uploadUrl,
            @NotNull URI contentLocation,
            @NotNull OffsetDateTime expiresAt,
            @NotNull Map<String, String> requiredHeaders) {
    }
}
