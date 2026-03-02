package com.deposition.domain.adapter.converter;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.deposition.domain.models.IntellectualEntityMetadata;
import com.deposition.domain.port.in.IntellectualEntityMetadataParam;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class IntellectualEntityParamConverter {
    public abstract void update(@MappingTarget IntellectualEntityMetadata intellectualEntityMetadata,
            IntellectualEntityMetadataParam intellectualEntityMetadataParam);
}
