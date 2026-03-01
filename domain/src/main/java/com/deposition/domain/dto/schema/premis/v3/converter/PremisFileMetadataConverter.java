package com.deposition.domain.dto.schema.premis.v3.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.deposition.domain.dto.schema.premis.v3.File;
import com.deposition.domain.models.FileMetadata;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = PremisCommonConverter.class)
public abstract class PremisFileMetadataConverter {
    @Autowired
    PremisCommonConverter commonConverter;

    @Mapping(target = "id", expression = "java(commonConverter.extractLocalObjectId(file.getObjectIdentifier()))")
    @Mapping(target = "originalName", source = "originalName.value")
    @Mapping(target = "identifiers", source = "objectIdentifier")
    @Mapping(target = "relationships", source = "relationship")
    @Mapping(target = "characteristics", ignore = true)
    @Mapping(target = "storages", ignore = true)
    public abstract FileMetadata map(File file);
}
