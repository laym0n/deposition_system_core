package com.deposition.domain.port.out;

import com.deposition.domain.models.IntellectualEntityType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntellectualEntityTypeOutPort {

    Optional<IntellectualEntityType> findById(UUID id);

    Optional<IntellectualEntityType> findByName(String name);

    List<IntellectualEntityType> findAll();

    IntellectualEntityType save(IntellectualEntityType type);

    void deleteById(UUID id);
}
