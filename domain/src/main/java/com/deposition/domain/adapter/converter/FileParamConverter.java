package com.deposition.domain.adapter.converter;

import com.deposition.domain.models.FileMetadata;
import com.deposition.domain.port.in.object.FileMetadataParam;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class FileParamConverter {
    public abstract void update(@MappingTarget FileMetadata fileMetadata, FileMetadataParam fileMetadataParam);
}
