package com.deposition.infra.micrometer.metrics;

import com.deposition.domain.port.out.BusinessMetricsOutPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class BusinessMetrics implements BusinessMetricsOutPort {

    private final Counter depositionOperationsTotal;
    private final Counter objectVersionUpdatesTotal;
    private final Counter proofRequestsTotal;
    private final Counter objectViewsTotal;
    private final Counter fileDownloadsTotal;

    public BusinessMetrics(MeterRegistry registry) {
        Objects.requireNonNull(registry, "registry must not be null");

        this.depositionOperationsTotal = Counter.builder("deposition.business.deposition_operations.total")
                .description("Total number of deposition operations (object deposit)")
                .register(registry);

        this.objectVersionUpdatesTotal = Counter.builder("deposition.business.object_version_updates.total")
                .description("Total number of object updates (new versions / metadata updates)")
                .register(registry);

        this.proofRequestsTotal = Counter.builder("deposition.business.proof_requests.total")
                .description("Total number of requests confirming deposition proof")
                .register(registry);

        this.objectViewsTotal = Counter.builder("deposition.business.object_views.total")
                .description("Total number of object views")
                .register(registry);

        this.fileDownloadsTotal = Counter.builder("deposition.business.file_downloads.total")
                .description("Total number of file downloads")
                .register(registry);
    }

    @Override
    public void incrementFileDownloads() {
        fileDownloadsTotal.increment();
    }

    @Override
    public void incrementDepositionOperations() {
        depositionOperationsTotal.increment();
    }

    @Override
    public void incrementObjectVersionUpdates() {
        objectVersionUpdatesTotal.increment();
    }

    @Override
    public void incrementProofRequests() {
        proofRequestsTotal.increment();
    }

    @Override
    public void incrementObjectViews() {
        objectViewsTotal.increment();
    }
}
