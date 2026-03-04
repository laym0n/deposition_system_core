package com.deposition.infra.relationaldb.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;
import com.deposition.infra.relationaldb.repository.DescriptiveMetadataSchemaJpaRepository;

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

        return repository.findByEntityTypeAndActiveIsTrue(entityType)
                .map(schema -> schema.getSchemaJson());
    }
}
