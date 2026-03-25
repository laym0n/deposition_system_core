package com.deposition.domain.port.in.object;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
public interface GetPremisMetadataInPort {

    Resource getPremisMetadata(@NotNull UUID objectId, @Nullable String versionId);
}
