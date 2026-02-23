package com.deposition.domain.dto.schema.premis.v3.converter;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.deposition.domain.dto.schema.premis.v3.EventComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectComplexType;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, uses = CommonConverter.class)
public interface PremisMetadataConverter {

    @Mapping(target = "version", constant = CommonConverter.PREMIS_VERSION)
    PremisComplexType map(List<ObjectComplexType> object, List<EventComplexType> event);
}