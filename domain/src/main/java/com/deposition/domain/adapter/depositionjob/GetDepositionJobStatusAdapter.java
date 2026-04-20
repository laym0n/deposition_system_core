package com.deposition.domain.adapter.depositionjob;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.depositionjob.GetDepositionJobStatusInPort;
import com.deposition.domain.port.out.DepositionJobOutPort;
import com.deposition.domain.port.out.UserOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Component
@Validated
@RequiredArgsConstructor
public class GetDepositionJobStatusAdapter implements GetDepositionJobStatusInPort {

    private final DepositionJobOutPort jobOutPort;
    private final UserOutPort userOutPort;

    @Override
    public DepositionJobStatusResponse getStatus(UUID jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }
        String currentUserId = userOutPort.getCurrentUserId();

        var job = jobOutPort.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("DepositionJob", jobId.toString()));
        if (!currentUserId.equals(job.ownerUserId())) {
            // Avoid information leak.
            throw new ResourceNotFoundException("DepositionJob", jobId.toString());
        }

        DepositionResult result = null;
        if (job.resultTxId() != null && !job.resultTxId().isBlank()) {
            result = new DepositionResult(job.objectId(), job.resultTxId(), job.resultVersionId());
        }

        return new DepositionJobStatusResponse(
                job.jobId(),
                job.objectId(),
                job.status(),
                job.createdAt(),
                job.updatedAt(),
                result,
                job.errorMessage()
        );
    }
}
