package com.deposition.domain.service;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.IntellectualEntityType;
import com.deposition.domain.port.out.IntellectualEntityTypeOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntellectualEntityTypeResolver {

    private final IntellectualEntityTypeOutPort outPort;

    public IntellectualEntityType resolveByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("entityTypeName must not be blank");
        }

        return outPort.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "IntellectualEntityType",
                        "name=" + name));
    }
}
