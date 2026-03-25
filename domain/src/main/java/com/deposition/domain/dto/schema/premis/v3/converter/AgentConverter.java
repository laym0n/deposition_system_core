package com.deposition.domain.dto.schema.premis.v3.converter;

import com.deposition.domain.dto.schema.premis.v3.AgentComplexType;
import com.deposition.domain.dto.schema.premis.v3.AgentIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.StringPlusAuthority;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.valueobject.AgentIdentifier;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, uses = CommonConverter.class)
public abstract class AgentConverter {

    @Autowired
    private CommonConverter commonConverter;

    @Mapping(target = "agentIdentifier", source = "identifiers")
    @Mapping(target = "agentName", source = "name", qualifiedByName = "toAgentNames")
    @Mapping(target = "agentType", source = "type")
    @Mapping(target = "version", constant = CommonConverter.PREMIS_VERSION)
    @Mapping(target = "xmlID", source = "id", qualifiedByName = "toXmlId")
    public abstract AgentComplexType map(AgentMetadata agentMetadata);

    @Mapping(target = "agentIdentifierType", source = "type")
    @Mapping(target = "agentIdentifierValue", source = "value")
    @Mapping(target = "simpleLink", ignore = true)
    protected abstract AgentIdentifierComplexType map(AgentIdentifier identifier);

    @Named("toAgentNames")
    protected List<StringPlusAuthority> toAgentNames(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        return List.of(commonConverter.toStringPlusAuthority(name));
    }

    @AfterMapping
    protected void convertNameToUpperCase(@MappingTarget AgentComplexType agentComplexType,
                                          AgentMetadata agentMetadata) {
        var agentIdentifiers = agentComplexType.getAgentIdentifier();

        var agentIdentifier = new AgentIdentifierComplexType();
        agentIdentifier.setAgentIdentifierType(commonConverter.toStringPlusAuthority(AgentIdentifierType.SYSTEM.name()));
        agentIdentifier.setAgentIdentifierValue(agentMetadata.getId().toString());
        agentIdentifiers.add(agentIdentifier);
    }
}
