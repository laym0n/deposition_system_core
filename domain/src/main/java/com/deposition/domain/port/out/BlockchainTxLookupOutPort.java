package com.deposition.domain.port.out;

import jakarta.annotation.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface BlockchainTxLookupOutPort {

    Optional<String> findTxId(UUID objectId, @Nullable String storageVersionId);
}
