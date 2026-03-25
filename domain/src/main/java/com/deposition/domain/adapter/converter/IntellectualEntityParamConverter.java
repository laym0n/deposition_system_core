package com.deposition.domain.adapter.converter;

import com.deposition.domain.models.IntellectualEntityMetadata;
import com.deposition.domain.port.in.object.IntellectualEntityMetadataParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class IntellectualEntityParamConverter {
    public abstract void update(@MappingTarget IntellectualEntityMetadata intellectualEntityMetadata,
                                IntellectualEntityMetadataParam intellectualEntityMetadataParam);
}
