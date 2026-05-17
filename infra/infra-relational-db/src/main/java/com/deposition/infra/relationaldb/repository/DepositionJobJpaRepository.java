package com.deposition.infra.relationaldb.repository;

import com.deposition.infra.relationaldb.entity.DepositionJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface DepositionJobJpaRepository extends JpaRepository<DepositionJobEntity, UUID> {

    Optional<DepositionJobEntity> findByOwnerUserIdAndIdempotencyKey(String ownerUserId, String idempotencyKey);

    Page<DepositionJobEntity> findAllByOwnerUserIdOrderByCreatedAtDesc(String ownerUserId, Pageable pageable);
}
