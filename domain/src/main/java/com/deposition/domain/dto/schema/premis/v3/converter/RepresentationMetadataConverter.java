package com.deposition.domain.dto.schema.premis.v3.converter;

import com.deposition.domain.dto.schema.premis.v3.Representation;
import com.deposition.domain.models.RepresentationMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, uses = CommonConverter.class)
public abstract class RepresentationMetadataConverter {

    @Mapping(target = "objectIdentifier", source = "identifiers")
    @Mapping(target = "relationship", source = "relationships")
    @Mapping(target = "version", constant = CommonConverter.PREMIS_VERSION)
    @Mapping(target = "xmlID", source = "id", qualifiedByName = "toXmlId")
    public abstract Representation map(RepresentationMetadata representationMetadata);
}
