package com.deposition.domain.adapter.depositionjob;

import com.deposition.domain.port.in.depositionjob.CreateDepositionJobInPort;
import com.deposition.domain.port.in.depositionjob.DepositionJobStatus;
import com.deposition.domain.models.depositionjob.DepositionJob;
import com.deposition.domain.models.depositionjob.DepositionJobFile;
import com.deposition.domain.port.out.DepositionJobOutPort;
import com.deposition.domain.port.out.PresignUploadOutPort;
import com.deposition.domain.port.out.UserOutPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Component
@Validated
@RequiredArgsConstructor
public class CreateDepositionJobAdapter implements CreateDepositionJobInPort {

    private static final Duration DEFAULT_PRESIGN_TTL = Duration.ofMinutes(15);

    private final DepositionJobOutPort jobOutPort;
    private final PresignUploadOutPort presignUploadOutPort;
    private final UserOutPort userOutPort;
    private final ObjectMapper objectMapper;

    @Override
    public CreateDepositionJobResult create(CreateDepositionJobCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        String ownerUserId = userOutPort.getCurrentUserId();

        // Idempotency: if client repeats the request with the same key, return the existing job.
        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            var existing = jobOutPort.findByOwnerAndIdempotencyKey(ownerUserId, command.idempotencyKey());
            if (existing.isPresent()) {
                var job = existing.get();
                var files = jobOutPort.listFiles(job.jobId());

                // We don't store the presigned URL in DB; so we re-presign for the same object keys.
                var uploads = files.stream()
                        .map(f -> {
                            var presigned = presignUploadOutPort.presignPutObject(
                                    new PresignUploadOutPort.PresignPutObjectCommand(
                                            f.objectKey(),
                                            f.contentType(),
                                            DEFAULT_PRESIGN_TTL));
                            return new PresignedUpload(
                                    f.fileId(),
                                    presigned.contentLocation(),
                                    f.objectKey(),
                                    presigned.uploadUrl(),
                                    presigned.expiresAt(),
                                    presigned.requiredHeaders());
                        })
                        .toList();

                return new CreateDepositionJobResult(job.jobId(), job.objectId(), job.status(), uploads);
            }
        }

        UUID jobId = UUID.randomUUID();
        UUID objectId = UUID.randomUUID();
        var now = OffsetDateTime.now(ZoneOffset.UTC);

        // Persist request as JSON for later processing.
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(command);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize deposition job request", ex);
        }

        var job = new DepositionJob(
                jobId,
                objectId,
                ownerUserId,
                DepositionJobStatus.UPLOADING,
                requestJson,
                command.idempotencyKey(),
                now,
                now,
                null,
                null,
                null
        );
        jobOutPort.create(job);

        // Generate pre-signed uploads and persist file records.
        List<DepositionJobFile> files = java.util.stream.IntStream
                .range(0, command.representations().size())
                .boxed()
                .flatMap(repIdx -> command.representations().get(repIdx).files().stream()
                        .map(f -> {
                            UUID fileId = UUID.randomUUID();
                            // Include representation index to avoid collisions across representations.
                            String objectKey = "object/" + objectId + "/rep-" + repIdx + "/" + f.originalName();

                            var presigned = presignUploadOutPort.presignPutObject(
                                    new PresignUploadOutPort.PresignPutObjectCommand(
                                            objectKey,
                                            f.contentType(),
                                            DEFAULT_PRESIGN_TTL));

                            return new DepositionJobFile(
                                    fileId,
                                    jobId,
                                    repIdx,
                                    f.originalName(),
                                    f.contentType(),
                                    f.sizeBytes(),
                                    objectKey,
                                    presigned.contentLocation()
                            );
                        }))
                .toList();
        jobOutPort.upsertFiles(jobId, files);

        var uploads = files.stream()
                .map(f -> {
                    var presigned = presignUploadOutPort.presignPutObject(
                            new PresignUploadOutPort.PresignPutObjectCommand(
                                    f.objectKey(),
                                    f.contentType(),
                                    DEFAULT_PRESIGN_TTL));
                    return new PresignedUpload(
                            f.fileId(),
                            presigned.contentLocation(),
                            f.objectKey(),
                            presigned.uploadUrl(),
                            presigned.expiresAt(),
                            presigned.requiredHeaders());
                })
                .toList();

        return new CreateDepositionJobResult(jobId, objectId, DepositionJobStatus.UPLOADING, uploads);
    }
}
