package com.deposition.domain.adapter.depositionjob;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.port.in.depositionjob.DepositionJobStatus;
import com.deposition.domain.port.in.depositionjob.ProcessDepositionJobInPort;
import com.deposition.domain.port.in.depositionjob.SubmitDepositionJobInPort;
import com.deposition.domain.port.out.DepositionJobOutPort;
import com.deposition.domain.port.out.UserOutPort;
import com.deposition.domain.service.acl.AccessValidatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
@Validated
@RequiredArgsConstructor
@Slf4j
public class SubmitDepositionJobAdapter implements SubmitDepositionJobInPort {

    private final DepositionJobOutPort jobOutPort;
    private final UserOutPort userOutPort;
    private final AccessValidatorService accessValidatorService;
    private final ObjectMapper objectMapper;
    private final ProcessDepositionJobInPort processDepositionJobInPort;

    @Override
    public void submit(UUID jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }

        String currentUserId = userOutPort.getCurrentUserId();
        var job = jobOutPort.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("DepositionJob", jobId.toString()));
        if (!currentUserId.equals(job.ownerUserId())) {
            throw new ResourceNotFoundException("DepositionJob", jobId.toString());
        }

        if (job.status() == DepositionJobStatus.COMPLETED
                || job.status() == DepositionJobStatus.CANCELLED) {
            return; // idempotent
        }

        // Validate that user can reference related objects (WRITE permission).
        // This must happen in request thread (security context is available here).
        validateRelationshipsOwnedByCurrentUser(job);

        var now = OffsetDateTime.now(ZoneOffset.UTC);
        var updated = new DepositionJobOutPort.DepositionJob(
                job.jobId(),
                job.objectId(),
                job.ownerUserId(),
                DepositionJobStatus.PROCESSING,
                job.requestJson(),
                job.idempotencyKey(),
                job.createdAt(),
                now,
                job.resultTxId(),
                job.resultVersionId(),
                job.errorMessage()
        );
        jobOutPort.update(updated);

        // Fire-and-forget processing. Status can be polled via GET /depone/jobs/{jobId}.
        processAsync(jobId);
    }

    @Async("depositionJobExecutor")
    void processAsync(UUID jobId) {
        try {
            processDepositionJobInPort.process(jobId);
        } catch (Exception e) {
            log.error(ExceptionUtils.getMessage(e), e);
        }
    }

    private void validateRelationshipsOwnedByCurrentUser(DepositionJobOutPort.DepositionJob job) {
        try {
            var cmd = objectMapper.readValue(
                    job.requestJson(),
                    com.deposition.domain.port.in.depositionjob.CreateDepositionJobInPort.CreateDepositionJobCommand.class);

            var meta = cmd.intellectualEntityMetadata();
            if (meta == null || meta.relationships() == null) {
                return;
            }

            meta.relationships().stream()
                    .filter(relationship -> relationship != null && relationship.getRelatedObjects() != null)
                    .flatMap(relationship -> relationship.getRelatedObjects().stream())
                    .filter(relatedObject -> relatedObject != null && relatedObject.getValue() != null
                            && !relatedObject.getValue().isBlank())
                    .forEach(relatedObject -> {
                        UUID objectId;
                        try {
                            objectId = UUID.fromString(relatedObject.getValue());
                        } catch (IllegalArgumentException ex) {
                            throw new IllegalArgumentException(
                                    "Invalid related object identifier (expected UUID): " + relatedObject.getValue());
                        }
                        accessValidatorService.validateCurrentUserHasPermission(objectId, AclPermission.WRITE);
                    });
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid deposition job requestJson", ex);
        }
    }
}
