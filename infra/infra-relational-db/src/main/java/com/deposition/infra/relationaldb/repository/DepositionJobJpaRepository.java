package com.deposition.infra.relationaldb.repository;

import com.deposition.infra.relationaldb.entity.DepositionJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DepositionJobJpaRepository extends JpaRepository<DepositionJobEntity, UUID> {

    Optional<DepositionJobEntity> findByOwnerUserIdAndIdempotencyKey(String ownerUserId, String idempotencyKey);

    java.util.List<DepositionJobEntity> findAllByOwnerUserIdOrderByCreatedAtDesc(String ownerUserId);
}
