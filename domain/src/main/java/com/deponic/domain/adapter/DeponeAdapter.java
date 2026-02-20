package com.deponic.domain.adapter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
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
        var allObjectManifestResources = new ArrayList<Resource>();
        var allEventResources = new ArrayList<Resource>();

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
            allObjectManifestResources.add(persistedFileMetadata.objectManifestResource());
            allEventResources.add(persistedFileMetadata.eventResource());
        }

        if (depositedFileObjectIds.isEmpty()) {
            return;
        }

        var representationMetadataStructure = MetadataBuilder.buildForRepresentation(depositedFileObjectIds);
        var persistedRepresentationMetadata = persistMetadataSet(representationMetadataStructure, "representation");
        allObjectManifestIds.add(persistedRepresentationMetadata.objectManifestId().toString());
        allEventIds.add(persistedRepresentationMetadata.eventId().toString());
        allObjectManifestResources.add(persistedRepresentationMetadata.objectManifestResource());
        allEventResources.add(persistedRepresentationMetadata.eventResource());

        var intellectualEntityMetadataStructure = MetadataBuilder.buildForIntellectualEntity(
                params.intellectualEntityMetadata(),
                representationMetadataStructure.objectManifest().getId());
        var persistedIntellectualEntityMetadata = persistMetadataSet(intellectualEntityMetadataStructure, "intellectual-entity");
        allObjectManifestIds.add(persistedIntellectualEntityMetadata.objectManifestId().toString());
        allEventIds.add(persistedIntellectualEntityMetadata.eventId().toString());
        allObjectManifestResources.add(persistedIntellectualEntityMetadata.objectManifestResource());
        allEventResources.add(persistedIntellectualEntityMetadata.eventResource());

        var objectRootHash = ResourceHashCalculator.calculateMerkleRootHash(allObjectManifestResources);
        var eventRootHash = ResourceHashCalculator.calculateMerkleRootHash(allEventResources);

        var snapshotManifest = buildSnapshotManifest(allObjectManifestIds, allEventIds, objectRootHash, eventRootHash);
        var snapshotResource = XmlUtils.createXmlResource(snapshotManifest, "snapshot-manifest");
        fileStorage.persist(snapshotResource);

        var anchorRecord = buildAnchorRecord(snapshotResource);
        anchorRecord = blockchain.persistAnchorRecord(anchorRecord);
        var snapshotPointer = SnapshotPointer.builder()
                .anchorRecordId(anchorRecord.getId())
                // .offChainLocation() TODO: set off-chain location
                .build();
        blockchain.persistSnapshotPoint(snapshotPointer);
    }

    private PersistedMetadataSet persistMetadataSet(MetadataBuilder.MetadataStructure metadataStructure, String sourceFilename) {
        var metadataResource = XmlUtils.createXmlResource(metadataStructure.objectMetadata(), sourceFilename);
        var metadataStorage = fileStorage.persist(metadataResource);
        metadataStructure.objectManifest().setObjectMetadataCids(List.of(metadataStorage));

        var eventResource = XmlUtils.createXmlResource(metadataStructure.creationEvent(), sourceFilename + ".event-creation");
        fileStorage.persist(eventResource);

        var objectManifestResource = XmlUtils.createXmlResource(metadataStructure.objectManifest(),
                sourceFilename + ".object-manifest");
        fileStorage.persist(objectManifestResource);

        return new PersistedMetadataSet(
                metadataStructure.objectManifest().getId(),
                metadataStructure.creationEvent().getId(),
                objectManifestResource,
                eventResource);
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

    private AnchorRecord buildAnchorRecord(Resource snapshotResource) {
        var snapshotHash = ResourceHashCalculator.sha256(snapshotResource);

        return AnchorRecord.builder()
                .id(UUID.randomUUID().toString())
                .snapshotHash(snapshotHash)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    private record PersistedMetadataSet(
            UUID objectManifestId,
            UUID eventId,
            Resource objectManifestResource,
            Resource eventResource) {
    }
}
