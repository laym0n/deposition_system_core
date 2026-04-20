package com.deposition.infra.relationaldb.repository;

import com.deposition.infra.relationaldb.entity.DepositionJobFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DepositionJobFileJpaRepository extends JpaRepository<DepositionJobFileEntity, UUID> {

    List<DepositionJobFileEntity> findAllByJobId(UUID jobId);
}
