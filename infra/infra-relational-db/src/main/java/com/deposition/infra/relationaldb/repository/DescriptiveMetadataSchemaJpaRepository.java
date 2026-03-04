package com.deposition.infra.relationaldb.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deposition.infra.relationaldb.entity.DescriptiveMetadataSchemaEntity;

public interface DescriptiveMetadataSchemaJpaRepository extends JpaRepository<DescriptiveMetadataSchemaEntity, UUID> {

    Optional<DescriptiveMetadataSchemaEntity> findByEntityTypeAndActiveIsTrue(String entityType);
}
