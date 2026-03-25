package com.deposition.domain.adapter.schema;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.DescriptiveMetadataSchema;
import com.deposition.domain.port.in.schema.UpdateDescriptiveMetadataSchemaActiveInPort;
import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@RequiredArgsConstructor
@Validated
public class UpdateDescriptiveMetadataSchemaActiveAdapter implements UpdateDescriptiveMetadataSchemaActiveInPort {

    private final DescriptiveMetadataSchemaOutPort outPort;

    @Override
    public DescriptiveMetadataSchema updateActive(java.util.UUID schemaId, UpdateActiveCommand command) {
        var existing = outPort.findById(schemaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DescriptiveMetadataSchema", schemaId.toString()));

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
