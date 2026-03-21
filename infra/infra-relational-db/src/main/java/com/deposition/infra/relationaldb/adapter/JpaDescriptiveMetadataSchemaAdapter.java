package com.deposition.infra.relationaldb.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.deposition.domain.models.DescriptiveMetadataSchema;
import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;
import com.deposition.infra.relationaldb.entity.DescriptiveMetadataSchemaEntity;
import com.deposition.infra.relationaldb.repository.DescriptiveMetadataSchemaJpaRepository;
import com.deposition.infra.relationaldb.repository.spec.DescriptiveMetadataSchemaSpecifications;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaDescriptiveMetadataSchemaAdapter implements DescriptiveMetadataSchemaOutPort {

    private final DescriptiveMetadataSchemaJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<String> findActiveSchemaJsonByEntityType(String entityType) {
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("entityType must not be blank");
        }

        return repository.findFirstByEntityTypeAndActiveIsTrueOrderByUpdatedAtDesc(entityType)
                .map(schema -> schema.getSchemaJson());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DescriptiveMetadataSchema> findById(UUID id) {
        return repository.findById(id).map(JpaDescriptiveMetadataSchemaAdapter::toDomain);
    }

    @Override
    @Transactional
    public DescriptiveMetadataSchema save(DescriptiveMetadataSchema schema) {
        if (schema == null) {
            throw new IllegalArgumentException("schema must not be null");
        }

        var entity = repository.findById(schema.id())
                .orElseGet(DescriptiveMetadataSchemaEntity::new);
        entity.setId(schema.id());
        entity.setEntityType(schema.entityType().name());
        entity.setSchemaJson(schema.schemaJson());
        entity.setActive(schema.active());

        // Hibernate will fill these, but keep them aligned when we update domain from DB.
        // For new entities, createdAt/updatedAt will be set by @CreationTimestamp/@UpdateTimestamp.
        var saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DescriptiveMetadataSchemaSummary> findSchemas(DescriptiveMetadataSchemaFilter filter) {
        var entityType = filter == null ? null : filter.entityType();
        var active = filter == null ? null : filter.active();

        Specification<com.deposition.infra.relationaldb.entity.DescriptiveMetadataSchemaEntity> spec
                = DescriptiveMetadataSchemaSpecifications.entityTypeEquals(entityType)
                        .and(DescriptiveMetadataSchemaSpecifications.activeEquals(active));

        var sort = Sort.by(
                Sort.Order.asc("entityType"),
                Sort.Order.asc("createdAt"));

        return repository.findAll(spec, sort).stream()
                .map(s -> new DescriptiveMetadataSchemaSummary(
                s.getId(),
                s.getEntityType(),
                s.isActive(),
                s.getCreatedAt(),
                s.getUpdatedAt()))
                .toList();
    }

    private static DescriptiveMetadataSchema toDomain(DescriptiveMetadataSchemaEntity entity) {
        return new DescriptiveMetadataSchema(
                entity.getId(),
                com.deposition.domain.port.in.schema.IntellectualEntityType.valueOf(entity.getEntityType()),
                entity.getSchemaJson(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
