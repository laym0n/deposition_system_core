package com.deponic.domain.adapter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.deponic.domain.models.Event;
import com.deponic.domain.models.ObjectManifest;
import com.deponic.domain.models.ObjectMetadata;
import com.deponic.domain.models.enums.EventObjectLinkRole;
import com.deponic.domain.models.enums.EventType;
import com.deponic.domain.models.enums.ObjectCategory;
import com.deponic.domain.models.enums.ObjectLinkSubType;
import com.deponic.domain.models.enums.ObjectLinkType;
import com.deponic.domain.models.valueobject.Characteristics;
import com.deponic.domain.models.valueobject.EventObjectLink;
import com.deponic.domain.models.valueobject.FixityBlock;
import com.deponic.domain.models.valueobject.ObjectEventLink;
import com.deponic.domain.models.valueobject.ObjectsLink;
import com.deponic.domain.models.valueobject.Storage;
import com.deponic.domain.port.in.DeponeFileParam;

public final class MetadataBuilder {

    private static final String HASH_ALGORITHM = "SHA-256";

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
        var calculatedHash = calculateHash(fileParam, HASH_ALGORITHM);
        var fixity = new FixityBlock();
        fixity.setAlgorithm(HASH_ALGORITHM);
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
            var messageDigest = MessageDigest.getInstance(hashAlgorithm);
            var totalBytes = 0L;

            try (var inputStream = fileParam.resource().getInputStream()) {
                var buffer = new byte[8192];
                var bytesRead = inputStream.read(buffer);
                while (bytesRead != -1) {
                    messageDigest.update(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    bytesRead = inputStream.read(buffer);
                }
            }

            var hash = HexFormat.of().formatHex(messageDigest.digest());
            return new HashCalculationResult(hash, totalBytes);
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Failed to calculate hash for deposition file", exception);
        }
    }

    public record MetadataStructure(ObjectMetadata objectMetadata, Event creationEvent, ObjectManifest objectManifest) {

    }

    private record HashCalculationResult(String hash, long size) {

    }
}
