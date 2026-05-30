package com.deposition.domain.port.in.object;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Входной порт: GetCachedObjectMetadataInPort.
 */
public interface GetCachedObjectMetadataInPort {

    CachedObjectMetadataResponse getCachedMetadata(@NotNull UUID objectId);
}
