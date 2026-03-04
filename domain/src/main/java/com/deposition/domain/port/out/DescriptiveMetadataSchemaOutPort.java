package com.deposition.domain.port.out;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;

public interface DescriptiveMetadataSchemaOutPort {

    Optional<String> findActiveSchemaJsonByEntityType(@NotBlank String entityType);
}
