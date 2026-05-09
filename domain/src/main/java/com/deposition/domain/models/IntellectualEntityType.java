package com.deposition.domain.models;

import java.util.UUID;

public record IntellectualEntityType(
        UUID id,
        String name,
        String description) {
}
