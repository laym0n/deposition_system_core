package com.deposition.domain.dto.schema.premis.v3.converter;

import java.time.OffsetDateTime;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.deposition.domain.dto.schema.premis.v3.Bitstream;
import com.deposition.domain.dto.schema.premis.v3.File;
import com.deposition.domain.dto.schema.premis.v3.IntellectualEntity;
import com.deposition.domain.dto.schema.premis.v3.ObjectComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.OriginalNameComplexType;
import com.deposition.domain.dto.schema.premis.v3.RelatedEventIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.RelatedObjectIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.RelationshipComplexType;
import com.deposition.domain.dto.schema.premis.v3.Representation;
import com.deposition.domain.dto.schema.premis.v3.StringPlusAuthority;
import com.deposition.domain.models.AbstractObjectMetadata;
import com.deposition.domain.models.enums.ObjectIdentifierType;
import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.models.valueobject.RelationEventIdentifier;
import com.deposition.domain.models.valueobject.RelationObjectIdentifier;
import com.deposition.domain.models.valueobject.Relationship;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR)
public abstract class CommonConverter {

    protected static final String PREMIS_VERSION = "3.0";

    @Mapping(target = "authority", ignore = true)
    @Mapping(target = "authorityURI", ignore = true)
    @Mapping(target = "valueURI", ignore = true)
    abstract StringPlusAuthority toStringPlusAuthority(String value);

    @Mapping(target = "objectIdentifierType", source = "type")
    @Mapping(target = "objectIdentifierValue", source = "value")
    @Mapping(target = "simpleLink", ignore = true)
    abstract ObjectIdentifierComplexType map(ObjectIdentifier objectIdentifier);

    @Mapping(target = "relationshipType", source = "type")
    @Mapping(target = "relationshipSubType", source = "subType")
    @Mapping(target = "relatedObjectIdentifier", source = "relatedObjects")
    @Mapping(target = "relatedEventIdentifier", source = "relatedEvents")
    @Mapping(target = "relatedEnvironmentCharacteristic", ignore = true)
    @Mapping(target = "relatedEnvironmentPurpose", ignore = true)
    protected abstract RelationshipComplexType map(Relationship relationship);

    @Mapping(target = "relatedObjectIdentifierType", source = "type")
    @Mapping(target = "relatedObjectIdentifierValue", source = "value")
    @Mapping(target = "relatedObjectSequence", source = "sequence")
    @Mapping(target = "relObjectXmlID", ignore = true)
    @Mapping(target = "simpleLink", ignore = true)
    protected abstract RelatedObjectIdentifierComplexType map(RelationObjectIdentifier relationObjectIdentifier);

    @Mapping(target = "relatedEventIdentifierType", source = "type")
    @Mapping(target = "relatedEventIdentifierValue", source = "value")
    @Mapping(target = "relatedEventSequence", source = "sequence")
    @Mapping(target = "relEventXmlID", ignore = true)
    @Mapping(target = "simpleLink", ignore = true)
    protected abstract RelatedEventIdentifierComplexType map(RelationEventIdentifier relationEventIdentifier);

    @Mapping(target = "value", source = "originalNameValue")
    @Mapping(target = "simpleLink", ignore = true)
    abstract OriginalNameComplexType toOriginalName(String originalNameValue);

    @AfterMapping
    protected void convertNameToUpperCase(@MappingTarget ObjectComplexType objectComplexType,
            AbstractObjectMetadata objectMetadata) {
        List<ObjectIdentifierComplexType> objectIdentifiers;
        switch (objectComplexType) {
            case File file ->
                objectIdentifiers = file.getObjectIdentifier();
            case IntellectualEntity intellectualEntity ->
                objectIdentifiers = intellectualEntity.getObjectIdentifier();
            case Representation representation ->
                objectIdentifiers = representation.getObjectIdentifier();
            case Bitstream bitstream ->
                objectIdentifiers = bitstream.getObjectIdentifier();
            default -> {
                return;
            }
        }
        var objectIdentifier = new ObjectIdentifierComplexType();
        objectIdentifier.setObjectIdentifierType(toStringPlusAuthority(ObjectIdentifierType.SYSTEM.name()));
        objectIdentifier.setObjectIdentifierValue(objectMetadata.getId().toString());
        objectIdentifiers.add(objectIdentifier);
    }

    @Named("toXmlId")
    protected String toXmlId(String id) {
        return "id_" + id;
    }

    @Named("mapToString")
    String mapToString(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toString();
    }
}
