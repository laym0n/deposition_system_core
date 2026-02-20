package com.deposition.domain.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.deposition.domain.models.Event;
import com.deposition.domain.models.ObjectManifest;
import com.deposition.domain.models.ObjectMetadata;
import com.deposition.domain.models.enums.EventObjectLinkRole;
import com.deposition.domain.models.enums.EventType;
import com.deposition.domain.models.enums.ObjectCategory;
import com.deposition.domain.models.enums.ObjectLinkSubType;
import com.deposition.domain.models.enums.ObjectLinkType;
import com.deposition.domain.models.valueobject.Characteristics;
import com.deposition.domain.models.valueobject.EventObjectLink;
import com.deposition.domain.models.valueobject.FixityBlock;
import com.deposition.domain.models.valueobject.ObjectEventLink;
import com.deposition.domain.models.valueobject.ObjectsLink;
import com.deposition.domain.models.valueobject.Storage;
import com.deposition.domain.port.in.DeponeFileParam;

public final class MetadataBuilder {

    private MetadataBuilder() {

    }

    public static MetadataStructure buildForFile(DeponeFileParam fileParam) {
        var objectManifestId = UUID.randomUUID();
        var eventId = UUID.randomUUID();

        var objectMetadata = buildFilePremisMetadata(fileParam);
        var creationEvent = Event.builder()
                .id(eventId)
                .type(EventType.CREATION)
                .eventObjectLinks(List.of(EventObjectLink.builder()
                        .objectId(objectManifestId)
                        .role(EventObjectLinkRole.OUTCOME)
                        .build()))
                .build();
        var objectManifest = ObjectManifest.builder()
                .id(objectManifestId)
                .objectEventLinks(List.of(ObjectEventLink.builder()
                        .eventId(eventId.toString())
                        .build()))
                .build();

        return new MetadataStructure(objectMetadata, creationEvent, objectManifest);
    }

    public static MetadataStructure buildForRepresentation(List<UUID> fileObjectIds) {
        var representationMetadata = new ObjectMetadata();
        representationMetadata.setId(UUID.randomUUID());
        representationMetadata.setCategory(ObjectCategory.REPRESENTATION);

        var representationLinks = toObjectsLinks(
                fileObjectIds,
                ObjectLinkType.STRUCTURAL,
                ObjectLinkSubType.HAS_PART);

        return buildForObject(representationMetadata, representationLinks);
    }

    public static MetadataStructure buildForIntellectualEntity(ObjectMetadata baseMetadata, UUID representationObjectId) {
        var intellectualEntityMetadata = baseMetadata == null ? new ObjectMetadata() : baseMetadata;
        if (intellectualEntityMetadata.getId() == null) {
            intellectualEntityMetadata.setId(UUID.randomUUID());
        }
        intellectualEntityMetadata.setCategory(ObjectCategory.INTELLECTUAL_ENTITY);

        var representationLink = toObjectsLinks(
                List.of(representationObjectId),
                ObjectLinkType.STRUCTURAL,
                ObjectLinkSubType.IS_REPRESENTED_BY);

        return buildForObject(intellectualEntityMetadata, representationLink);
    }

    private static ObjectMetadata buildFilePremisMetadata(DeponeFileParam fileParam) {
        var calculatedHash = calculateHash(fileParam, ResourceHashCalculator.DEFAULT_HASH_ALGORITHM);
        var fixity = new FixityBlock();
        fixity.setAlgorithm(ResourceHashCalculator.DEFAULT_HASH_ALGORITHM);
        fixity.setDigest(calculatedHash.hash());

        var characteristics = new Characteristics();
        characteristics.setSize(calculatedHash.size());
        characteristics.setFixity(List.of(fixity));

        var storages = new ArrayList<Storage>();
        if (fileParam.storages() != null) {
            storages.addAll(fileParam.storages());
        }

        var objectMetadata = new ObjectMetadata();
        objectMetadata.setCategory(ObjectCategory.FILE);
        objectMetadata.setOriginalName(fileParam.resource().getFilename());
        objectMetadata.setCharacteristics(List.of(characteristics));
        objectMetadata.setStorages(storages);
        objectMetadata.setId(UUID.randomUUID());
        return objectMetadata;
    }

    private static MetadataStructure buildForObject(ObjectMetadata objectMetadata, List<ObjectsLink> objectsLinks) {
        var objectManifestId = UUID.randomUUID();
        var eventId = UUID.randomUUID();

        var creationEvent = Event.builder()
                .id(eventId)
                .type(EventType.CREATION)
                .eventObjectLinks(List.of(EventObjectLink.builder()
                        .objectId(objectManifestId)
                        .role(EventObjectLinkRole.OUTCOME)
                        .build()))
                .build();

        var objectManifest = ObjectManifest.builder()
                .id(objectManifestId)
                .objectsLinks(objectsLinks)
                .objectEventLinks(List.of(ObjectEventLink.builder()
                        .eventId(eventId.toString())
                        .build()))
                .build();

        return new MetadataStructure(objectMetadata, creationEvent, objectManifest);
    }

    private static List<ObjectsLink> toObjectsLinks(
            List<UUID> linkedObjectIds,
            ObjectLinkType linkType,
            ObjectLinkSubType linkSubType) {
        if (linkedObjectIds == null || linkedObjectIds.isEmpty()) {
            return List.of();
        }

        return linkedObjectIds.stream()
                .map(objectId -> ObjectsLink.builder()
                .objectId(objectId.toString())
                .type(linkType)
                .subType(linkSubType)
                .build())
                .collect(Collectors.toList());
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

    public record MetadataStructure(ObjectMetadata objectMetadata, Event creationEvent, ObjectManifest objectManifest) {

    }

    private record HashCalculationResult(String hash, long size) {

    }
}
