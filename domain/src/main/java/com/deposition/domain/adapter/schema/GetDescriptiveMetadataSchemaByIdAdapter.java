package com.deposition.domain.adapter.schema;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.DescriptiveMetadataSchema;
import com.deposition.domain.port.in.schema.GetDescriptiveMetadataSchemaByIdInPort;
import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class GetDescriptiveMetadataSchemaByIdAdapter implements GetDescriptiveMetadataSchemaByIdInPort {

    private final DescriptiveMetadataSchemaOutPort schemaOutPort;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getSchema(UUID schemaId) {
        DescriptiveMetadataSchema schema = schemaOutPort.findById(schemaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                "DescriptiveMetadataSchema", schemaId.toString()));

        try {
            return objectMapper.readValue(schema.schemaJson(), new TypeReference<Map<String, Object>>() {
            });
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Invalid descriptive metadata schema JSON for id=" + schemaId
                    + ": " + ex.getOriginalMessage(),
                    ex);
        }
    }
}
