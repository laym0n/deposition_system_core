package com.deposition.domain.adapter.schema;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.port.in.schema.GetDescriptiveMetadataSchemaInPort;
import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Validated
public class GetDescriptiveMetadataSchemaAdapter implements GetDescriptiveMetadataSchemaInPort {

    private final DescriptiveMetadataSchemaOutPort schemaOutPort;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getSchema(String entityTypeName) {
        if (entityTypeName == null || entityTypeName.isBlank()) {
            throw new IllegalArgumentException("entityTypeName must not be blank");
        }

        var schemaJson = schemaOutPort.findActiveSchemaJsonByEntityType(entityTypeName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DescriptiveMetadataSchema", "entityType=" + entityTypeName));

        try {
            return objectMapper.readValue(schemaJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Invalid descriptive metadata schema JSON for entityType=" + entityTypeName
                            + ": " + ex.getOriginalMessage(),
                    ex);
        }
    }
}
