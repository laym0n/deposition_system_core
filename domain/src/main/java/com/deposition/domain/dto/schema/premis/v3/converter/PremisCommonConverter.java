package com.deposition.domain.dto.schema.premis.v3.converter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.deposition.domain.dto.schema.premis.v3.LinkingAgentIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.RelatedEventIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.RelatedObjectIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.RelationshipComplexType;
import com.deposition.domain.dto.schema.premis.v3.StringPlusAuthority;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.enums.EventIdentifierType;
import com.deposition.domain.models.enums.ObjectIdentifierType;
import com.deposition.domain.models.enums.ObjectRelationshipSubType;
import com.deposition.domain.models.enums.ObjectRelationshipType;
import com.deposition.domain.models.valueobject.AgentIdentifier;
import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.models.valueobject.RelationEventIdentifier;
import com.deposition.domain.models.valueobject.RelationObjectIdentifier;
import com.deposition.domain.models.valueobject.Relationship;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class PremisCommonConverter {

    protected String unwrapStringPlusAuthority(StringPlusAuthority value) {
        if (value == null) {
            return null;
        }
        return value.getValue();
    }

    @Named("toObjectIdentifierType")
    protected ObjectIdentifierType toObjectIdentifierType(StringPlusAuthority value) {
        var raw = unwrapStringPlusAuthority(value);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ObjectIdentifierType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ObjectIdentifierType.OTHER;
        }
    }

    @Named("toObjectRelationshipType")
    protected ObjectRelationshipType toObjectRelationshipType(StringPlusAuthority value) {
        var raw = unwrapStringPlusAuthority(value);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return ObjectRelationshipType.valueOf(raw.toUpperCase());
    }

    @Named("toObjectRelationshipSubType")
    protected ObjectRelationshipSubType toObjectRelationshipSubType(StringPlusAuthority value) {
        var raw = unwrapStringPlusAuthority(value);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return ObjectRelationshipSubType.valueOf(raw.toUpperCase());
    }

    @Mapping(target = "type", source = "objectIdentifierType", qualifiedByName = "toObjectIdentifierType")
    @Mapping(target = "value", source = "objectIdentifierValue")
    public abstract ObjectIdentifier map(ObjectIdentifierComplexType objectIdentifierComplexType);

    @Mapping(target = "type", source = "relationshipType", qualifiedByName = "toObjectRelationshipType")
    @Mapping(target = "subType", source = "relationshipSubType", qualifiedByName = "toObjectRelationshipSubType")
    @Mapping(target = "relatedObjects", source = "relatedObjectIdentifier")
    @Mapping(target = "relatedEvents", source = "relatedEventIdentifier")
    protected abstract Relationship map(RelationshipComplexType relationshipComplexType);

    @Mapping(target = "type", source = "relatedObjectIdentifierType", qualifiedByName = "toObjectIdentifierType")
    @Mapping(target = "value", source = "relatedObjectIdentifierValue")
    @Mapping(target = "sequence", source = "relatedObjectSequence")
    protected abstract RelationObjectIdentifier map(RelatedObjectIdentifierComplexType relatedObjectIdentifier);

    @Mapping(target = "type", source = "relatedEventIdentifierType", qualifiedByName = "toEventIdentifierType")
    @Mapping(target = "value", source = "relatedEventIdentifierValue")
    @Mapping(target = "sequence", source = "relatedEventSequence")
    protected abstract RelationEventIdentifier map(RelatedEventIdentifierComplexType relatedEventIdentifier);

    @Named("toEventIdentifierType")
    protected EventIdentifierType toEventIdentifierType(StringPlusAuthority value) {
        var raw = unwrapStringPlusAuthority(value);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return EventIdentifierType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return EventIdentifierType.SYSTEM;
        }
    }

    @Named("toAgentIdentifierType")
    protected AgentIdentifierType toAgentIdentifierType(StringPlusAuthority value) {
        var raw = unwrapStringPlusAuthority(value);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return AgentIdentifierType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return AgentIdentifierType.SYSTEM;
        }
    }

    @Mapping(target = "type", source = "linkingAgentIdentifierType", qualifiedByName = "toAgentIdentifierType")
    @Mapping(target = "value", source = "linkingAgentIdentifierValue")
    protected abstract AgentIdentifier map(LinkingAgentIdentifierComplexType linkingAgentIdentifierComplexType);

    @Named("mapFromString")
    OffsetDateTime mapFromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return OffsetDateTime.parse(value);
    }

    protected UUID extractLocalObjectId(List<ObjectIdentifierComplexType> objectIdentifiers) {
        if (objectIdentifiers == null) {
            return null;
        }

        for (var objectIdentifier : objectIdentifiers) {
            if (objectIdentifier == null || objectIdentifier.getObjectIdentifierType() == null) {
                continue;
            }
            var type = unwrapStringPlusAuthority(objectIdentifier.getObjectIdentifierType());
            if (!Objects.equals(ObjectIdentifierType.SYSTEM.name(), type)) {
                continue;
            }
            var value = objectIdentifier.getObjectIdentifierValue();
            if (value == null || value.isBlank()) {
                continue;
            }
            return UUID.fromString(value);
        }

        return null;
    }
}
