package com.deposition.domain.port.out;

public interface BusinessMetricsOutPort {

    void incrementDepositionOperations();

    void incrementObjectVersionUpdates();

    void incrementProofRequests();

    void incrementObjectViews();

    void incrementFileDownloads();
}
