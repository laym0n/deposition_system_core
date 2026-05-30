package com.deposition.domain.port.out;

import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface ObjectIndexLookupOutPort {

    Optional<ObjectIndexDocument> findByObjectId(@NotNull UUID objectId);
}
