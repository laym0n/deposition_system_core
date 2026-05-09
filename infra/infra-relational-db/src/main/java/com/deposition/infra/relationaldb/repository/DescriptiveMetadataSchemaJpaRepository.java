package com.deposition.infra.relationaldb.repository;

import com.deposition.infra.relationaldb.entity.DescriptiveMetadataSchemaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DescriptiveMetadataSchemaJpaRepository extends JpaRepository<DescriptiveMetadataSchemaEntity, UUID>,
        JpaSpecificationExecutor<DescriptiveMetadataSchemaEntity> {

    Optional<DescriptiveMetadataSchemaEntity> findByEntityType_NameAndActiveIsTrue(String entityTypeName);

    Optional<DescriptiveMetadataSchemaEntity> findFirstByEntityType_NameAndActiveIsTrueOrderByUpdatedAtDesc(String entityTypeName);
}
