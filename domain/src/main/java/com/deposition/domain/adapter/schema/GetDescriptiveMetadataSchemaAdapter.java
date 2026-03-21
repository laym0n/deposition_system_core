package com.deposition.domain.adapter.schema;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.port.in.GetDescriptiveMetadataSchemaInPort;
import com.deposition.domain.port.in.IntellectualEntityType;
import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class GetDescriptiveMetadataSchemaAdapter implements GetDescriptiveMetadataSchemaInPort {

    private final DescriptiveMetadataSchemaOutPort schemaOutPort;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getSchema(IntellectualEntityType entityType) {
        var schemaJson = schemaOutPort.findActiveSchemaJsonByEntityType(entityType.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                "DescriptiveMetadataSchema", "entityType=" + entityType));

        try {
            return objectMapper.readValue(schemaJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Invalid descriptive metadata schema JSON for entityType=" + entityType
                    + ": " + ex.getOriginalMessage(),
                    ex);
        }
    }
}
