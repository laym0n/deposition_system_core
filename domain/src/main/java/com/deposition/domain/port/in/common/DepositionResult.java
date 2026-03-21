package com.deposition.domain.port.in.common;

import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record DepositionResult(
        @NotNull
        UUID objectId,
        @NotNull
        String txId,
        @Nullable
        String versionId) {

}
