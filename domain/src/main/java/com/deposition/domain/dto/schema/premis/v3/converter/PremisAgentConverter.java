package com.deposition.domain.dto.schema.premis.v3.converter;

import com.deposition.domain.dto.schema.premis.v3.AgentComplexType;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.enums.AgentType;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = PremisCommonConverter.class)
public abstract class PremisAgentConverter {

    @Autowired
    protected PremisCommonConverter commonConverter;

    @Mapping(target = "id", source = "xmlID")
    @Mapping(target = "name", expression = "java(extractAgentName(agentComplexType))")
    @Mapping(target = "type", expression = "java(extractAgentType(agentComplexType))")
    @Mapping(target = "identifiers", ignore = true)
    public abstract AgentMetadata map(AgentComplexType agentComplexType);

    protected AgentType extractAgentType(AgentComplexType agentComplexType) {
        if (agentComplexType == null || agentComplexType.getAgentType() == null) {
            return null;
        }
        var value = agentComplexType.getAgentType().getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        return AgentType.valueOf(value.toUpperCase());
    }

    protected String extractAgentName(AgentComplexType agentComplexType) {
        if (agentComplexType == null || agentComplexType.getAgentName() == null || agentComplexType.getAgentName().isEmpty()) {
            return null;
        }
        var first = agentComplexType.getAgentName().getFirst();
        return first == null ? null : first.getValue();
    }

    @AfterMapping
    protected void convertNameToUpperCase(@MappingTarget AgentMetadata agentMetadata, AgentComplexType agentComplexType) {
        var identifiers = agentMetadata.getIdentifiers().stream().filter(identifier -> identifier.getType() != AgentIdentifierType.SYSTEM).toList();
        agentMetadata.setIdentifiers(identifiers);
    }
}
