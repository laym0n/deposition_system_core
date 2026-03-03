package com.deposition.domain.port.in;

import java.util.UUID;

import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

@Validated
public interface VerifyPremisInPort {

    VerifyPremisResult verifyPremis(@NotNull UUID objectId, @Nullable String versionId);
}
