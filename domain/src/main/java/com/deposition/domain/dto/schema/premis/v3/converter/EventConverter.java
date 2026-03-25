package com.deposition.domain.dto.schema.premis.v3.converter;

import com.deposition.domain.dto.schema.premis.v3.*;
import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.enums.EventIdentifierType;
import com.deposition.domain.models.valueobject.EventAgentLink;
import com.deposition.domain.models.valueobject.EventDetailInformation;
import com.deposition.domain.models.valueobject.EventObjectLink;
import com.deposition.domain.models.valueobject.EventOutcomeInformation;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, uses = CommonConverter.class)
public abstract class EventConverter {

    @Autowired
    private CommonConverter commonConverter;

    @Mapping(target = "version", constant = CommonConverter.PREMIS_VERSION)
    @Mapping(target = "xmlID", source = "id", qualifiedByName = "toXmlId")
    @Mapping(target = "eventType", source = "type")
    @Mapping(target = "eventDateTime", source = "dateTime", qualifiedByName = "mapToString")
    @Mapping(target = "eventOutcomeInformation", source = "outcome")
    @Mapping(target = "eventDetailInformation", source = "detail")
    @Mapping(target = "eventIdentifier.eventIdentifierType", source = "identifier.type")
    @Mapping(target = "eventIdentifier.eventIdentifierValue", source = "identifier.value")
    @Mapping(target = "linkingObjectIdentifier", source = "objectLinks")
    @Mapping(target = "linkingAgentIdentifier", source = "agentLinks")
    public abstract EventComplexType map(EventMetadata eventMetadata);

    @Mapping(target = "eventOutcome", source = "outcome")
    @Mapping(target = "eventOutcomeDetail", source = "outcomeDetail")
    protected abstract EventOutcomeInformationComplexType map(EventOutcomeInformation information);

    @Mapping(target = "eventDetail", source = "detail")
    protected abstract EventDetailInformationComplexType map(EventDetailInformation detailInformation);

    @Mapping(target = "linkingAgentIdentifierType", source = "agentIdentifier.type")
    @Mapping(target = "linkingAgentIdentifierValue", source = "agentIdentifier.value")
    @Mapping(target = "linkingAgentRole", source = "role")
    protected abstract LinkingAgentIdentifierComplexType map(EventAgentLink agentLink);

    @Mapping(target = "linkingObjectIdentifierType", source = "objectIdentifier.type")
    @Mapping(target = "linkingObjectIdentifierValue", source = "objectIdentifier.value")
    @Mapping(target = "linkingObjectRole", source = "role")
    protected abstract LinkingObjectIdentifierComplexType map(EventObjectLink objectLink);

    @AfterMapping
    protected void convertNameToUpperCase(@MappingTarget EventComplexType eventComplexType,
                                          EventMetadata eventMetadata) {
        var eventIdentifier = new EventIdentifierComplexType();
        eventIdentifier.setEventIdentifierType(commonConverter.toStringPlusAuthority(EventIdentifierType.SYSTEM.name()));
        eventIdentifier.setEventIdentifierValue(eventMetadata.getId().toString());
        eventComplexType.setEventIdentifier(eventIdentifier);
    }
}
