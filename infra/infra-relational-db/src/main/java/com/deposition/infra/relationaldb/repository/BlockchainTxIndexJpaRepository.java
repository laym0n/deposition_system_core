package com.deposition.infra.relationaldb.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deposition.infra.relationaldb.entity.BlockchainTxIndexEntity;

public interface BlockchainTxIndexJpaRepository extends JpaRepository<BlockchainTxIndexEntity, UUID> {

    Optional<BlockchainTxIndexEntity> findByObjectIdAndStorageVersionId(UUID objectId, String storageVersionId);

    Optional<BlockchainTxIndexEntity> findByObjectIdAndStorageVersionIdIsNull(UUID objectId);
}
