package com.deposition.domain.adapter.converter;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.deposition.domain.models.FileMetadata;
import com.deposition.domain.port.in.FileMetadataParam;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR)
public abstract class FileParamConverter {
    public abstract void update(@MappingTarget FileMetadata fileMetadata, FileMetadataParam fileMetadataParam);
}
