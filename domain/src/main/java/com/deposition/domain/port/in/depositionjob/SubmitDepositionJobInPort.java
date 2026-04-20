package com.deposition.domain.port.in.depositionjob;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface SubmitDepositionJobInPort {

    void submit(@NotNull UUID jobId);
}
