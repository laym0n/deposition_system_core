package com.deposition.infra.relationaldb.adapter;

import com.deposition.domain.models.IntellectualEntityType;
import com.deposition.domain.port.out.IntellectualEntityTypeOutPort;
import com.deposition.infra.relationaldb.entity.IntellectualEntityTypeEntity;
import com.deposition.infra.relationaldb.repository.IntellectualEntityTypeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaIntellectualEntityTypeAdapter implements IntellectualEntityTypeOutPort {

    private final IntellectualEntityTypeJpaRepository repository;

    private static IntellectualEntityType toDomain(IntellectualEntityTypeEntity e) {
        if (e == null) {
            return null;
        }
        return new IntellectualEntityType(e.getId(), e.getName(), e.getDescription());
    }

    private static IntellectualEntityTypeEntity toEntity(IntellectualEntityType type) {
        if (type == null) {
            return null;
        }
        var e = new IntellectualEntityTypeEntity();
        e.setId(type.id());
        e.setName(type.name());
        e.setDescription(type.description());
        return e;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IntellectualEntityType> findById(UUID id) {
        return repository.findById(id).map(JpaIntellectualEntityTypeAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IntellectualEntityType> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return repository.findByName(name).map(JpaIntellectualEntityTypeAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntellectualEntityType> findAll() {
        return repository.findAll().stream().map(JpaIntellectualEntityTypeAdapter::toDomain).toList();
    }

    @Override
    @Transactional
    public IntellectualEntityType save(IntellectualEntityType type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (type.id() == null) {
            throw new IllegalArgumentException("type.id must not be null");
        }
        if (type.name() == null || type.name().isBlank()) {
            throw new IllegalArgumentException("type.name must not be blank");
        }

        var e = repository.findById(type.id()).orElseGet(IntellectualEntityTypeEntity::new);
        e.setId(type.id());
        e.setName(type.name());
        e.setDescription(type.description());
        return toDomain(repository.save(e));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
