package com.deposition.infra.relationaldb.repository;

import com.deposition.infra.relationaldb.entity.IntellectualEntityTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IntellectualEntityTypeJpaRepository extends JpaRepository<IntellectualEntityTypeEntity, UUID> {

    Optional<IntellectualEntityTypeEntity> findByName(String name);
}
