package com.deposition.domain.adapter;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.DescriptiveMetadataSchemaNotFoundException;
import com.deposition.domain.models.DescriptiveMetadataSchema;
import com.deposition.domain.port.in.UpdateDescriptiveMetadataSchemaActiveInPort;
import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class UpdateDescriptiveMetadataSchemaActiveAdapter implements UpdateDescriptiveMetadataSchemaActiveInPort {

    private final DescriptiveMetadataSchemaOutPort outPort;

    @Override
    public DescriptiveMetadataSchema updateActive(java.util.UUID schemaId, UpdateActiveCommand command) {
        var existing = outPort.findById(schemaId)
                .orElseThrow(() -> new DescriptiveMetadataSchemaNotFoundException(
                "Descriptive metadata schema not found: id=" + schemaId));

        var updated = new DescriptiveMetadataSchema(
                existing.id(),
                existing.entityType(),
                existing.schemaJson(),
                command.active(),
                existing.createdAt(),
                java.time.OffsetDateTime.now());

        return outPort.save(updated);
    }
}
