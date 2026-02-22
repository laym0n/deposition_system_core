package com.deposition.domain.adapter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.deposition.domain.dto.schema.premis.v3.ContentLocationComplexType;
import com.deposition.domain.dto.schema.premis.v3.EventComplexType;
import com.deposition.domain.dto.schema.premis.v3.EventIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.File;
import com.deposition.domain.dto.schema.premis.v3.FixityComplexType;
import com.deposition.domain.dto.schema.premis.v3.FormatComplexType;
import com.deposition.domain.dto.schema.premis.v3.FormatDesignationComplexType;
import com.deposition.domain.dto.schema.premis.v3.IntellectualEntity;
import com.deposition.domain.dto.schema.premis.v3.LinkingObjectIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectCharacteristicsComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectFactory;
import com.deposition.domain.dto.schema.premis.v3.ObjectIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.RelatedObjectIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.RelationshipComplexType;
import com.deposition.domain.dto.schema.premis.v3.Representation;
import com.deposition.domain.dto.schema.premis.v3.StorageComplexType;
import com.deposition.domain.dto.schema.premis.v3.StringPlusAuthority;
import com.deposition.domain.models.ObjectMetadata;
import com.deposition.domain.models.valueobject.Storage;
import com.deposition.domain.port.in.DeponeFileParam;

public final class MetadataBuilder {

    private static final String PREMIS_VERSION = "3.0";
    private static final String IDENTIFIER_TYPE_LOCAL = "LOCAL";
    private static final String CREATION_EVENT_TYPE = "CREATION";
    private static final String OUTCOME_LINK_ROLE = "OUTCOME";
    private static final String RELATIONSHIP_TYPE_STRUCTURAL = "STRUCTURAL";
    private static final String RELATIONSHIP_SUBTYPE_HAS_PART = "HAS_PART";
    private static final String RELATIONSHIP_SUBTYPE_IS_REPRESENTED_BY = "IS_REPRESENTED_BY";
    private static final String DEFAULT_FORMAT_NAME = "application/octet-stream";
    private static final String XML_ID_PREFIX = "id_";
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private MetadataBuilder() {

    }

    public static MetadataStructure buildForFile(DeponeFileParam fileParam) {
        var objectId = UUID.randomUUID();
        var eventId = UUID.randomUUID();

        var objectMetadata = buildFileObject(fileParam, objectId);
        var creationEvent = buildCreationEvent(eventId, objectId);

        return new MetadataStructure(objectId, objectMetadata, creationEvent);
    }

    public static MetadataStructure buildForRepresentation(List<UUID> fileObjectIds) {
        var objectId = UUID.randomUUID();
        var eventId = UUID.randomUUID();

        var representationMetadata = buildRepresentationObject(objectId);
        appendRelationship(
                representationMetadata.getRelationship(),
                RELATIONSHIP_TYPE_STRUCTURAL,
                RELATIONSHIP_SUBTYPE_HAS_PART,
                fileObjectIds);
        var creationEvent = buildCreationEvent(eventId, objectId);

        return new MetadataStructure(objectId, representationMetadata, creationEvent);
    }

    public static MetadataStructure buildForIntellectualEntity(ObjectMetadata baseMetadata,
            UUID representationObjectId) {
        var objectId = UUID.randomUUID();
        var eventId = UUID.randomUUID();

        var intellectualEntityMetadata = buildIntellectualEntityObject(baseMetadata, objectId);
        appendRelationship(
                intellectualEntityMetadata.getRelationship(),
                RELATIONSHIP_TYPE_STRUCTURAL,
                RELATIONSHIP_SUBTYPE_IS_REPRESENTED_BY,
                List.of(representationObjectId));
        var creationEvent = buildCreationEvent(eventId, objectId);

        return new MetadataStructure(objectId, intellectualEntityMetadata, creationEvent);
    }

    private static File buildFileObject(DeponeFileParam fileParam, UUID objectId) {
        var fileObject = new File();
        fileObject.setVersion(PREMIS_VERSION);
        fileObject.setXmlID(toXmlId(objectId.toString()));
        fileObject.getObjectIdentifier().add(createObjectIdentifier(objectId.toString()));

        var calculatedHash = calculateHash(fileParam, ResourceHashCalculator.DEFAULT_HASH_ALGORITHM);
        var characteristics = new ObjectCharacteristicsComplexType();
        characteristics.setSize(calculatedHash.size());
        characteristics.getFixity().add(toFixity(ResourceHashCalculator.DEFAULT_HASH_ALGORITHM, calculatedHash.hash()));
        characteristics.getFormat().add(toDefaultFormat());
        fileObject.getObjectCharacteristics().add(characteristics);

        if (fileParam.resource().getFilename() != null && !fileParam.resource().getFilename().isBlank()) {
            var originalName = OBJECT_FACTORY.createOriginalNameComplexType();
            originalName.setValue(fileParam.resource().getFilename());
            fileObject.setOriginalName(originalName);
        }

        if (fileParam.storages() != null) {
            fileParam.storages().stream()
                    .map(MetadataBuilder::toStorage)
                    .forEach(fileObject.getStorage()::add);
        }

        return fileObject;
    }

    private static Representation buildRepresentationObject(UUID objectId) {
        var representation = new Representation();
        representation.setVersion(PREMIS_VERSION);
        representation.setXmlID(toXmlId(objectId.toString()));
        representation.getObjectIdentifier().add(createObjectIdentifier(objectId.toString()));
        return representation;
    }

    private static IntellectualEntity buildIntellectualEntityObject(ObjectMetadata baseMetadata, UUID objectId) {
        var intellectualEntity = new IntellectualEntity();
        intellectualEntity.setVersion(PREMIS_VERSION);
        intellectualEntity.setXmlID(toXmlId(objectId.toString()));
        intellectualEntity.getObjectIdentifier().add(createObjectIdentifier(objectId.toString()));

        if (baseMetadata != null && baseMetadata.getOriginalName() != null
                && !baseMetadata.getOriginalName().isBlank()) {
            var originalName = OBJECT_FACTORY.createOriginalNameComplexType();
            originalName.setValue(baseMetadata.getOriginalName());
            intellectualEntity.setOriginalName(originalName);
        }

        return intellectualEntity;
    }

    private static EventComplexType buildCreationEvent(UUID eventId, UUID objectId) {
        var event = new EventComplexType();
        event.setVersion(PREMIS_VERSION);
        event.setXmlID(toXmlId(eventId.toString()));
        event.setEventIdentifier(createEventIdentifier(eventId.toString()));
        event.setEventType(toStringPlusAuthority(CREATION_EVENT_TYPE));
        event.setEventDateTime(OffsetDateTime.now().toString());
        event.getLinkingObjectIdentifier()
                .add(toLinkingObjectIdentifier(objectId.toString(), OUTCOME_LINK_ROLE));
        return event;
    }

    public static PremisComplexType buildPremis(List<MetadataStructure> metadataStructures) {
        var premis = new PremisComplexType();
        premis.setVersion(PREMIS_VERSION);

        if (metadataStructures == null || metadataStructures.isEmpty()) {
            return premis;
        }

        for (var metadataStructure : metadataStructures) {
            if (metadataStructure == null) {
                continue;
            }

            if (metadataStructure.objectMetadata() != null) {
                premis.getObject().add(metadataStructure.objectMetadata());
            }

            if (metadataStructure.creationEvent() != null) {
                premis.getEvent().add(metadataStructure.creationEvent());
            }
        }

        return premis;
    }

    public static void appendStorage(ObjectComplexType objectMetadata, Storage storage) {
        if (objectMetadata == null) {
            return;
        }

        if (objectMetadata instanceof File fileObject) {
            fileObject.getStorage().add(toStorage(storage));
            return;
        }

        if (objectMetadata instanceof Representation representation) {
            representation.getStorage().add(toStorage(storage));
        }
    }

    public static String extractEventId(EventComplexType event) {
        if (event == null) {
            return UUID.randomUUID().toString();
        }

        if (event.getEventIdentifier() == null || event.getEventIdentifier().getEventIdentifierValue() == null) {
            return UUID.randomUUID().toString();
        }
        return event.getEventIdentifier().getEventIdentifierValue();
    }

    private static FixityComplexType toFixity(String algorithm, String digest) {
        var premisFixity = new FixityComplexType();
        premisFixity.setMessageDigestAlgorithm(toStringPlusAuthority(algorithm));
        premisFixity.setMessageDigest(digest);
        return premisFixity;
    }

    private static FormatComplexType toDefaultFormat() {
        var format = new FormatComplexType();
        var formatDesignation = new FormatDesignationComplexType();
        formatDesignation.setFormatName(toStringPlusAuthority(DEFAULT_FORMAT_NAME));
        format.getContent().add(OBJECT_FACTORY.createFormatDesignation(formatDesignation));
        return format;
    }

    private static StorageComplexType toStorage(Storage storage) {
        var premisStorage = new StorageComplexType();
        if (storage == null || storage.getContentLocation() == null) {
            return premisStorage;
        }

        var contentLocation = new ContentLocationComplexType();
        contentLocation
                .setContentLocationType(toStringPlusAuthority(storage.getContentLocation().getContentLocationType()));
        contentLocation.setContentLocationValue(storage.getContentLocation().getContentLocationValue());
        premisStorage.getContent().add(OBJECT_FACTORY.createContentLocation(contentLocation));
        return premisStorage;
    }

    private static EventIdentifierComplexType createEventIdentifier(String eventId) {
        var identifier = new EventIdentifierComplexType();
        identifier.setEventIdentifierType(toStringPlusAuthority(IDENTIFIER_TYPE_LOCAL));
        identifier.setEventIdentifierValue(eventId);
        return identifier;
    }

    private static LinkingObjectIdentifierComplexType toLinkingObjectIdentifier(String objectId, String role) {
        var linkingObjectIdentifier = new LinkingObjectIdentifierComplexType();
        linkingObjectIdentifier.setLinkingObjectIdentifierType(toStringPlusAuthority(IDENTIFIER_TYPE_LOCAL));
        linkingObjectIdentifier.setLinkingObjectIdentifierValue(objectId);

        if (role != null && !role.isBlank()) {
            linkingObjectIdentifier.getLinkingObjectRole().add(toStringPlusAuthority(role));
        }

        return linkingObjectIdentifier;
    }

    private static ObjectIdentifierComplexType createObjectIdentifier(String objectId) {
        var identifier = new ObjectIdentifierComplexType();
        identifier.setObjectIdentifierType(toStringPlusAuthority(IDENTIFIER_TYPE_LOCAL));
        identifier.setObjectIdentifierValue(objectId);
        return identifier;
    }

    private static StringPlusAuthority toStringPlusAuthority(String value) {
        var stringPlusAuthority = new StringPlusAuthority();
        stringPlusAuthority.setValue(value);
        return stringPlusAuthority;
    }

    private static String toXmlId(String rawId) {
        return XML_ID_PREFIX + rawId;
    }

    private static void appendRelationship(
            List<RelationshipComplexType> targetRelationships,
            String relationshipType,
            String relationshipSubType,
            List<UUID> relatedObjectIds) {
        if (targetRelationships == null || relatedObjectIds == null || relatedObjectIds.isEmpty()) {
            return;
        }

        var relationship = new RelationshipComplexType();
        relationship.setRelationshipType(toStringPlusAuthority(relationshipType));
        relationship.setRelationshipSubType(toStringPlusAuthority(relationshipSubType));

        for (var relatedObjectId : relatedObjectIds) {
            if (relatedObjectId == null) {
                continue;
            }

            var relatedObjectIdentifier = new RelatedObjectIdentifierComplexType();
            relatedObjectIdentifier.setRelatedObjectIdentifierType(toStringPlusAuthority(IDENTIFIER_TYPE_LOCAL));
            relatedObjectIdentifier.setRelatedObjectIdentifierValue(relatedObjectId.toString());
            relationship.getRelatedObjectIdentifier().add(relatedObjectIdentifier);
        }

        if (!relationship.getRelatedObjectIdentifier().isEmpty()) {
            targetRelationships.add(relationship);
        }
    }

    private static HashCalculationResult calculateHash(DeponeFileParam fileParam, String hashAlgorithm) {
        try {
            var hash = ResourceHashCalculator.calculateHash(fileParam.resource(), hashAlgorithm);
            var totalBytes = fileParam.resource().contentLength();
            return new HashCalculationResult(hash, totalBytes);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to calculate hash for deposition file", exception);
        }
    }

    public record MetadataStructure(
            UUID objectId,
            ObjectComplexType objectMetadata,
            EventComplexType creationEvent) {

    }

    private record HashCalculationResult(String hash, long size) {

    }
}
