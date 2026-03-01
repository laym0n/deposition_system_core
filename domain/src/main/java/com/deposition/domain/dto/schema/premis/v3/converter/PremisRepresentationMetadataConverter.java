package com.deposition.domain.dto.schema.premis.v3.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.deposition.domain.dto.schema.premis.v3.Representation;
import com.deposition.domain.models.RepresentationMetadata;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = PremisCommonConverter.class)
public abstract class PremisRepresentationMetadataConverter {
    @Autowired
    PremisCommonConverter commonConverter;

    @Mapping(target = "id", expression = "java(commonConverter.extractLocalObjectId(representation.getObjectIdentifier()))")
    @Mapping(target = "originalName", source = "originalName.value")
    @Mapping(target = "identifiers", source = "objectIdentifier")
    @Mapping(target = "relationships", source = "relationship")
    public abstract RepresentationMetadata map(Representation representation);
}
