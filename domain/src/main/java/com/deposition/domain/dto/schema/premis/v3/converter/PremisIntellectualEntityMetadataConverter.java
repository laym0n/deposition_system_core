package com.deposition.domain.dto.schema.premis.v3.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.deposition.domain.dto.schema.premis.v3.IntellectualEntity;
import com.deposition.domain.models.IntellectualEntityMetadata;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = PremisCommonConverter.class)
public abstract class PremisIntellectualEntityMetadataConverter {
    @Autowired
    PremisCommonConverter commonConverter;

    @Mapping(target = "id", expression = "java(commonConverter.extractLocalObjectId(intellectualEntity.getObjectIdentifier()))")
    @Mapping(target = "originalName", source = "originalName.value")
    @Mapping(target = "identifiers", source = "objectIdentifier")
    @Mapping(target = "relationships", source = "relationship")
    public abstract IntellectualEntityMetadata map(IntellectualEntity intellectualEntity);
}
