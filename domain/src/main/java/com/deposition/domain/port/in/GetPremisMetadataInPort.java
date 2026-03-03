package com.deposition.domain.port.in;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

@Validated
public interface GetPremisMetadataInPort {

    Resource getPremisMetadata(@NotNull UUID objectId, @Nullable String versionId);
}
