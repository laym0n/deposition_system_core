package com.deposition.domain.port.in;

import java.util.UUID;

import com.deposition.domain.port.in.dto.CachedObjectMetadataResponse;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

/**
 * Returns object metadata from OpenSearch cache.
 */
public interface GetCachedObjectMetadataInPort {

    CachedObjectMetadataResponse getCachedMetadata(@NotNull UUID objectId, @Nullable String currentUserId);
}
