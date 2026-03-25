package com.deposition.domain.dto.schema.premis.v3.converter;

import com.deposition.domain.dto.schema.premis.v3.*;
import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.enums.EventAgentLinkRole;
import com.deposition.domain.models.enums.EventObjectLinkRole;
import com.deposition.domain.models.enums.EventType;
import com.deposition.domain.models.valueobject.EventAgentLink;
import com.deposition.domain.models.valueobject.EventIdentifier;
import com.deposition.domain.models.valueobject.EventObjectLink;
import com.deposition.domain.models.valueobject.ObjectIdentifier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = PremisCommonConverter.class)
public abstract class PremisEventConverter {

    @Autowired
    protected PremisCommonConverter commonConverter;

    @Mapping(target = "id", expression = "java(parseEventId(eventComplexType))")
    @Mapping(target = "type", source = "eventType", qualifiedByName = "toEventType")
    @Mapping(target = "dateTime", source = "eventDateTime", qualifiedByName = "mapFromString")
    @Mapping(target = "outcome", ignore = true)
    @Mapping(target = "detail", ignore = true)
    @Mapping(target = "identifier", source = "eventIdentifier")
    @Mapping(target = "objectLinks", source = "linkingObjectIdentifier")
    @Mapping(target = "agentLinks", source = "linkingAgentIdentifier")
    public abstract EventMetadata map(EventComplexType eventComplexType);

    @Mapping(target = "agentIdentifier", source = ".")
    @Mapping(target = "role", source = "linkingAgentRole", qualifiedByName = "toEventAgentLinkRoles")
    protected abstract EventAgentLink map(LinkingAgentIdentifierComplexType complex);

    @Mapping(target = "objectIdentifier", source = ".", qualifiedByName = "mapObjectIdentifier")
    @Mapping(target = "role", source = "linkingObjectRole", qualifiedByName = "toEventObjectLinkRoles")
    protected abstract EventObjectLink map(LinkingObjectIdentifierComplexType complex);

    @Named("mapObjectIdentifier")
    protected ObjectIdentifier mapObjectIdentifier(LinkingObjectIdentifierComplexType complex) {
        if (complex == null) {
            return null;
        }
        return ObjectIdentifier.builder()
                .type(commonConverter.toObjectIdentifierType(complex.getLinkingObjectIdentifierType()))
                .value(complex.getLinkingObjectIdentifierValue())
                .build();
    }

    @Named("toEventType")
    protected EventType toEventType(StringPlusAuthority value) {
        var raw = commonConverter.unwrapStringPlusAuthority(value);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return EventType.valueOf(raw.toUpperCase());
    }

    @Named("toEventObjectLinkRoles")
    protected List<EventObjectLinkRole> toEventObjectLinkRoles(List<StringPlusAuthority> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        var result = new ArrayList<EventObjectLinkRole>(roles.size());
        for (var role : roles) {
            var value = commonConverter.unwrapStringPlusAuthority(role);
            if (value == null || value.isBlank()) {
                continue;
            }
            result.add(EventObjectLinkRole.valueOf(value.toUpperCase()));
        }
        return Collections.unmodifiableList(result);
    }

    @Named("toEventAgentLinkRoles")
    protected List<EventAgentLinkRole> toEventAgentLinkRoles(List<StringPlusAuthority> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        var result = new ArrayList<EventAgentLinkRole>(roles.size());
        for (var role : roles) {
            var value = commonConverter.unwrapStringPlusAuthority(role);
            if (value == null || value.isBlank()) {
                continue;
            }
            result.add(EventAgentLinkRole.valueOf(value.toUpperCase()));
        }
        return Collections.unmodifiableList(result);
    }

    @Mapping(target = "type", source = "eventIdentifierType", qualifiedByName = "toEventIdentifierType")
    @Mapping(target = "value", source = "eventIdentifierValue")
    protected abstract EventIdentifier map(EventIdentifierComplexType complex);

    protected UUID parseEventId(EventComplexType eventComplexType) {
        if (eventComplexType == null || eventComplexType.getEventIdentifier() == null) {
            return null;
        }
        var value = eventComplexType.getEventIdentifier().getEventIdentifierValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        return UUID.fromString(value);
    }
}
