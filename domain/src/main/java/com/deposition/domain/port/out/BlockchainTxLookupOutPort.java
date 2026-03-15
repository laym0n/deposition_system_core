package com.deposition.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import jakarta.annotation.Nullable;

public interface BlockchainTxLookupOutPort {

    Optional<String> findTxId(UUID objectId, @Nullable String storageVersionId);
}
