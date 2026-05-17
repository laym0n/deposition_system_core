package com.deposition.domain.adapter.depositionjob;

import com.deposition.domain.port.in.depositionjob.ListMyDepositionJobsInPort;
import com.deposition.domain.port.in.depositionjob.CreateDepositionJobInPort;
import com.deposition.domain.port.out.DepositionJobOutPort;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.domain.port.out.UserOutPort;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private CreateDepositionJobInPort.CreateDepositionJobCommand tryParseRequestJson(String requestJson) {
        if (requestJson == null || requestJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(requestJson, CreateDepositionJobInPort.CreateDepositionJobCommand.class);
        } catch (Exception e) {
            // Best-effort fallback: listing should not fail if historical requestJson is malformed.
            return null;
        }
    }

    @Override
    public DepositionJobPage listMyJobs(ListMyJobsQuery query) {
        String currentUserId = userOutPort.getCurrentUserId();

        var page = jobOutPort.listByOwnerUserId(currentUserId, query.page(), query.size());

        var items = page.items()
                .stream()
                .map(j -> {
                    // Prefer OpenSearch cached metadata; fall back to job.requestJson (if index is stale).
                    var doc = objectIndexLookupOutPort.findByObjectId(j.objectId()).orElse(null);
                    var cmd = tryParseRequestJson(j.requestJson());

                    String objectName = firstNonBlank(
                            doc == null || doc.premis() == null ? null : doc.premis().originalName(),
                            cmd == null || cmd.intellectualEntityMetadata() == null ? null : cmd.intellectualEntityMetadata().originalName()
                    );

                    String intellectualEntityTypeName = firstNonBlank(
                            doc == null ? null : doc.intellectualEntityTypeName(),
                            cmd == null ? null : cmd.intellectualEntityTypeName()
                    );

                    return new DepositionJobListItem(
                            j.jobId(),
                            j.objectId(),
                            objectName,
                            intellectualEntityTypeName,
                            j.status(),
                            j.createdAt(),
                            j.updatedAt()
                    );
                })
                .toList();

        return new DepositionJobPage(items, query.page(), query.size(), page.totalItems());
    }
}
