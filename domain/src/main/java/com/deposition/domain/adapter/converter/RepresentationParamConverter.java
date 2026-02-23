package com.deposition.domain.adapter.converter;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.deposition.domain.models.RepresentationMetadata;
import com.deposition.domain.port.in.RepresentationMetadataParam;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR)
public abstract class RepresentationParamConverter {
    public abstract void update(@MappingTarget RepresentationMetadata representationMetadata,
            RepresentationMetadataParam representationMetadataParam);
}
