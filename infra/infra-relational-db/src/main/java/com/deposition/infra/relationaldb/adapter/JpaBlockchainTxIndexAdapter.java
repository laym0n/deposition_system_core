package com.deposition.infra.relationaldb.adapter;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.deposition.domain.port.out.BlockchainTxIndexOutPort;
import com.deposition.infra.relationaldb.entity.BlockchainTxIndexEntity;
import com.deposition.infra.relationaldb.repository.BlockchainTxIndexJpaRepository;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaBlockchainTxIndexAdapter implements BlockchainTxIndexOutPort {

    private final BlockchainTxIndexJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findTxId(UUID objectId, @Nullable String storageVersionId) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }

        return findRow(objectId, storageVersionId).map(BlockchainTxIndexEntity::getTxId);
    }

    @Override
    @Transactional
    public void save(UUID objectId, @Nullable String storageVersionId, String txId) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (txId == null || txId.isBlank()) {
            throw new IllegalArgumentException("txId must not be blank");
        }

        var row = findRow(objectId, storageVersionId).orElseGet(() -> {
            var entity = new BlockchainTxIndexEntity();
            entity.setId(deterministicId(objectId, storageVersionId));
            entity.setObjectId(objectId);
            entity.setStorageVersionId(storageVersionId);
            return entity;
        });

        row.setTxId(txId);
        repository.saveAndFlush(row);
    }

    private Optional<BlockchainTxIndexEntity> findRow(UUID objectId, @Nullable String storageVersionId) {
        if (storageVersionId == null) {
            return repository.findByObjectIdAndStorageVersionIdIsNull(objectId);
        }
        return repository.findByObjectIdAndStorageVersionId(objectId, storageVersionId);
    }

    private static UUID deterministicId(UUID objectId, @Nullable String storageVersionId) {
        var raw = objectId.toString() + ":" + (storageVersionId == null ? "<null>" : storageVersionId);
        return UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8));
    }
}
