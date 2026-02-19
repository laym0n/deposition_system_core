package com.deponic.domain.adapter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.deponic.domain.models.AnchorRecord;
import com.deponic.domain.models.Snapshot;
import com.deponic.domain.models.SnapshotPointer;
import com.deponic.domain.models.valueobject.SnapshotEventLink;
import com.deponic.domain.models.valueobject.SnapshotObjectLink;
import com.deponic.domain.port.in.DeponeFileParam;
import com.deponic.domain.port.in.DeponeInPort;
import com.deponic.domain.port.in.DeponeObjectParams;
import com.deponic.domain.port.out.BlockchainOutPort;
import com.deponic.domain.port.out.FileStorageOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeponeAdapter implements DeponeInPort {

    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;

    @Override
    public void depone(DeponeObjectParams params) {
        if (params == null || params.files() == null) {
            return;
        }

        var depositedFileObjectIds = new ArrayList<UUID>();
        var allObjectManifestIds = new ArrayList<String>();
        var allEventIds = new ArrayList<String>();
        var allObjectManifestXmlPayloads = new ArrayList<byte[]>();
        var allEventXmlPayloads = new ArrayList<byte[]>();

        for (var fileParam : params.files()) {
            if (fileParam == null || fileParam.resource() == null) {
                continue;
            }

            var fileStorageLocation = fileStorage.persist(fileParam.resource());
            var storages = new ArrayList<>(fileParam.storages() == null ? List.of() : fileParam.storages());
            storages.add(fileStorageLocation);

            var depositionFileParam = new DeponeFileParam(fileParam.resource(), storages);
            var metadataStructure = MetadataBuilder.buildForFile(depositionFileParam);
            var persistedFileMetadata = persistMetadataSet(metadataStructure, fileParam.resource().getFilename());

            depositedFileObjectIds.add(persistedFileMetadata.objectManifestId());
            allObjectManifestIds.add(persistedFileMetadata.objectManifestId().toString());
            allEventIds.add(persistedFileMetadata.eventId().toString());
            allObjectManifestXmlPayloads.add(persistedFileMetadata.objectManifestXmlPayload());
            allEventXmlPayloads.add(persistedFileMetadata.eventXmlPayload());
        }

        if (depositedFileObjectIds.isEmpty()) {
            return;
        }

        var representationMetadataStructure = MetadataBuilder.buildForRepresentation(depositedFileObjectIds);
        var persistedRepresentationMetadata = persistMetadataSet(representationMetadataStructure, "representation");
        allObjectManifestIds.add(persistedRepresentationMetadata.objectManifestId().toString());
        allEventIds.add(persistedRepresentationMetadata.eventId().toString());
        allObjectManifestXmlPayloads.add(persistedRepresentationMetadata.objectManifestXmlPayload());
        allEventXmlPayloads.add(persistedRepresentationMetadata.eventXmlPayload());

        var intellectualEntityMetadataStructure = MetadataBuilder.buildForIntellectualEntity(
                params.intellectualEntityMetadata(),
                representationMetadataStructure.objectManifest().getId());
        var persistedIntellectualEntityMetadata = persistMetadataSet(intellectualEntityMetadataStructure, "intellectual-entity");
        allObjectManifestIds.add(persistedIntellectualEntityMetadata.objectManifestId().toString());
        allEventIds.add(persistedIntellectualEntityMetadata.eventId().toString());
        allObjectManifestXmlPayloads.add(persistedIntellectualEntityMetadata.objectManifestXmlPayload());
        allEventXmlPayloads.add(persistedIntellectualEntityMetadata.eventXmlPayload());

        var objectRootHash = MerkleProof.calculateRootHash(allObjectManifestXmlPayloads);
        var eventRootHash = MerkleProof.calculateRootHash(allEventXmlPayloads);

        var snapshotManifest = buildSnapshotManifest(allObjectManifestIds, allEventIds, objectRootHash, eventRootHash);
        var snapshotXmlPayload = XmlFileBuilder.buildXmlBytes(snapshotManifest);
        var snapshotResource = XmlFileBuilder.createXmlResource(snapshotXmlPayload, "snapshot-manifest");
        fileStorage.persist(snapshotResource);

        var anchorRecord = buildAnchorRecord(snapshotXmlPayload);
        anchorRecord = blockchain.persistAnchorRecord(anchorRecord);
        var snapshotPointer = SnapshotPointer.builder()
                .anchorRecordId(anchorRecord.getId())
                // .offChainLocation() TODO: set off-chain location
                .build();
        blockchain.persistSnapshotPoint(snapshotPointer);
    }

    private PersistedMetadataSet persistMetadataSet(MetadataBuilder.MetadataStructure metadataStructure, String sourceFilename) {
        var metadataXmlPayload = XmlFileBuilder.buildXmlBytes(metadataStructure.objectMetadata());
        var metadataResource = XmlFileBuilder.createXmlResource(metadataXmlPayload, sourceFilename);
        var metadataStorage = fileStorage.persist(metadataResource);
        metadataStructure.objectManifest().setObjectMetadataCids(List.of(metadataStorage));

        var eventXmlPayload = XmlFileBuilder.buildXmlBytes(metadataStructure.creationEvent());
        var eventResource = XmlFileBuilder.createXmlResource(eventXmlPayload, sourceFilename + ".event-creation");
        fileStorage.persist(eventResource);

        var objectManifestXmlPayload = XmlFileBuilder.buildXmlBytes(metadataStructure.objectManifest());
        var objectManifestResource = XmlFileBuilder.createXmlResource(objectManifestXmlPayload, sourceFilename + ".object-manifest");
        fileStorage.persist(objectManifestResource);

        return new PersistedMetadataSet(
                metadataStructure.objectManifest().getId(),
                metadataStructure.creationEvent().getId(),
                objectManifestXmlPayload,
                eventXmlPayload);
    }

    private Snapshot buildSnapshotManifest(
            List<String> objectManifestIds,
            List<String> eventIds,
            String objectRootHash,
            String eventRootHash) {
        var objectLinks = objectManifestIds.stream()
                .map(objectManifestId -> SnapshotObjectLink.builder()
                .objectManifestId(objectManifestId)
                .build())
                .collect(Collectors.toList());

        var eventLinks = eventIds.stream()
                .map(eventId -> SnapshotEventLink.builder()
                .eventId(eventId)
                .build())
                .collect(Collectors.toList());

        return Snapshot.builder()
                .id(UUID.randomUUID().toString())
                .objectLinks(objectLinks)
                .objectRootHash(objectRootHash)
                .eventLinks(eventLinks)
                .eventRootHash(eventRootHash)
                .build();
    }

    private AnchorRecord buildAnchorRecord(byte[] snapshotXmlPayload) {
        var snapshotHash = MerkleProof.calculateRootHash(List.of(snapshotXmlPayload));

        return AnchorRecord.builder()
                .id(UUID.randomUUID().toString())
                .snapshotHash(snapshotHash)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    private record PersistedMetadataSet(
            UUID objectManifestId,
            UUID eventId,
            byte[] objectManifestXmlPayload,
            byte[] eventXmlPayload) {

    }
}
