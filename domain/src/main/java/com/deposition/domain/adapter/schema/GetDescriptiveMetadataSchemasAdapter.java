package com.deposition.domain.adapter.schema;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.port.in.GetDescriptiveMetadataSchemasInPort;
import com.deposition.domain.port.in.IntellectualEntityType;
import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class GetDescriptiveMetadataSchemasAdapter implements GetDescriptiveMetadataSchemasInPort {

    private final DescriptiveMetadataSchemaOutPort schemaOutPort;

    @Override
    public List<DescriptiveMetadataSchemaSummary> getSchemas(DescriptiveMetadataSchemaFilter filter) {
        var outFilter = new DescriptiveMetadataSchemaOutPort.DescriptiveMetadataSchemaFilter(
                filter == null || filter.entityType() == null ? null : filter.entityType().name(),
                filter == null ? null : filter.active());

        return schemaOutPort.findSchemas(outFilter)
                .stream()
                .map(s -> new DescriptiveMetadataSchemaSummary(s.id(),
                IntellectualEntityType.valueOf(s.entityType()),
                s.active(),
                s.createdAt(),
                s.updatedAt()))
                .toList();
    }
}
