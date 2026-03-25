package com.deposition.domain.port.out;

import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Read-only access to object index cached in OpenSearch.
 */
public interface ObjectIndexLookupOutPort {

    Optional<ObjectIndexDocument> findByObjectId(@NotNull UUID objectId);
}
