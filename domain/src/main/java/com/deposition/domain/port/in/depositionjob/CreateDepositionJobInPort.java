package com.deposition.domain.port.in.depositionjob;

import com.deposition.domain.port.in.object.IntellectualEntityMetadataParam;
import com.deposition.domain.port.in.object.RepresentationMetadataParam;
import com.deposition.domain.port.in.schema.IntellectualEntityType;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CreateDepositionJobInPort {

    CreateDepositionJobResult create(@NotNull @Valid CreateDepositionJobCommand command);

    record CreateDepositionJobCommand(
            /** Optional idempotency key. Usually comes from Idempotency-Key header. */
            @Nullable
            String idempotencyKey,
            @NotNull
            IntellectualEntityType intellectualEntityType,
            @Nullable
            @Valid
            IntellectualEntityMetadataParam intellectualEntityMetadata,
            @Nullable
            String descriptiveMetadata,
            @Nullable
            @Valid
            RepresentationMetadataParam representationMetadata,
            @NotEmpty
            List<@Valid DepositionJobFileUploadParam> files) {
    }

    record DepositionJobFileUploadParam(
            @NotNull
            String originalName,
            @Nullable
            String contentType,
            @Nullable
            Long sizeBytes) {
    }

    record CreateDepositionJobResult(
            @NotNull
            UUID jobId,
            @NotNull
            UUID objectId,
            @NotNull
            DepositionJobStatus status,
            @NotNull
            List<PresignedUpload> uploads) {
    }

    record PresignedUpload(
            @NotNull
            UUID fileId,
            @NotNull
            URI contentLocation,
            @NotNull
            String objectKey,
            @NotNull
            URI uploadUrl,
            @NotNull
            OffsetDateTime expiresAt,
            @NotNull
            Map<String, String> requiredHeaders) {
    }
}
