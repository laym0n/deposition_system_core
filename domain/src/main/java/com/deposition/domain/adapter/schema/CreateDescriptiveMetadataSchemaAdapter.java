package com.deposition.domain.adapter.schema;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.models.DescriptiveMetadataSchema;
import com.deposition.domain.port.in.schema.CreateDescriptiveMetadataSchemaInPort;
import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class CreateDescriptiveMetadataSchemaAdapter implements CreateDescriptiveMetadataSchemaInPort {

    private final DescriptiveMetadataSchemaOutPort outPort;
    private final ObjectMapper objectMapper;

    @Override
    public DescriptiveMetadataSchema create(CreateDescriptiveMetadataSchemaCommand command) {
        validateJson(command.schemaJson());

        var now = OffsetDateTime.now();
        var schema = new DescriptiveMetadataSchema(
                UUID.randomUUID(),
                command.entityType(),
                command.schemaJson(),
                true,
                now,
                now);

        return outPort.save(schema);
    }

    private void validateJson(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("schemaJson must not be blank");
        }
        try {
            objectMapper.readTree(json);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid schemaJson: " + ex.getOriginalMessage(), ex);
        }
    }
}
