package com.deposition.infra.relationaldb.repository;

import com.deposition.infra.relationaldb.entity.DescriptiveMetadataSchemaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DescriptiveMetadataSchemaJpaRepository extends JpaRepository<DescriptiveMetadataSchemaEntity, UUID>,
        JpaSpecificationExecutor<DescriptiveMetadataSchemaEntity> {

    Optional<DescriptiveMetadataSchemaEntity> findByEntityTypeAndActiveIsTrue(String entityType);

    Optional<DescriptiveMetadataSchemaEntity> findFirstByEntityTypeAndActiveIsTrueOrderByUpdatedAtDesc(String entityType);
}
