package com.deposition.domain.adapter.converter;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.deposition.domain.models.RepresentationMetadata;
import com.deposition.domain.port.in.object.RepresentationMetadataParam;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class RepresentationParamConverter {
    public abstract void update(@MappingTarget RepresentationMetadata representationMetadata,
            RepresentationMetadataParam representationMetadataParam);
}
