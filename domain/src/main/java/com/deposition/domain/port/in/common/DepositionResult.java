package com.deposition.domain.port.in.common;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DepositionResult(
        @NotNull
        UUID objectId,
        @NotNull
        String txId,
        @Nullable
        String versionId) {

}
