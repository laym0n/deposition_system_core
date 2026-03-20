package com.deposition.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Read-only access to object index cached in OpenSearch.
 */
public interface ObjectIndexLookupOutPort {

    Optional<ObjectIndexDocument> findByObjectId(@NotNull UUID objectId);
}
