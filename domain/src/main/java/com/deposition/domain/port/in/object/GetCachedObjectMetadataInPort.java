package com.deposition.domain.port.in.object;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Returns object metadata from OpenSearch cache.
 */
public interface GetCachedObjectMetadataInPort {

    CachedObjectMetadataResponse getCachedMetadata(@NotNull UUID objectId);
}
