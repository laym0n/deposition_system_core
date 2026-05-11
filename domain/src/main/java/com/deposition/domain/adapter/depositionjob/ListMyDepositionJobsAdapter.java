package com.deposition.domain.adapter.depositionjob;

import com.deposition.domain.port.in.depositionjob.ListMyDepositionJobsInPort;
import com.deposition.domain.port.out.DepositionJobOutPort;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.domain.port.out.UserOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@RequiredArgsConstructor
public class ListMyDepositionJobsAdapter implements ListMyDepositionJobsInPort {

    private final DepositionJobOutPort jobOutPort;
    private final ObjectIndexLookupOutPort objectIndexLookupOutPort;
    private final UserOutPort userOutPort;

    @Override
    public java.util.List<DepositionJobListItem> listMyJobs() {
        String currentUserId = userOutPort.getCurrentUserId();
        return jobOutPort.listByOwnerUserId(currentUserId)
                .stream()
                .map(j -> new DepositionJobListItem(
                        j.jobId(),
                        j.objectId(),
                        objectIndexLookupOutPort.findByObjectId(j.objectId())
                                .map(d -> d.premis() == null ? null : d.premis().originalName())
                                .orElse(null),
                        objectIndexLookupOutPort.findByObjectId(j.objectId())
                                .map(com.deposition.domain.port.out.ObjectIndexDocument::intellectualEntityTypeName)
                                .orElse(null),
                        j.status(),
                        j.createdAt(),
                        j.updatedAt()
                ))
                .toList();
    }
}
