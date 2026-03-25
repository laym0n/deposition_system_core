package com.deposition.domain.dto.schema.premis.v3.converter;

import com.deposition.domain.dto.schema.premis.v3.AgentComplexType;
import com.deposition.domain.dto.schema.premis.v3.EventComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectComplexType;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, uses = CommonConverter.class)
public interface PremisMetadataConverter {

    @Mapping(target = "version", constant = CommonConverter.PREMIS_VERSION)
    PremisComplexType map(List<ObjectComplexType> object, List<EventComplexType> event, List<AgentComplexType> agent);
}
