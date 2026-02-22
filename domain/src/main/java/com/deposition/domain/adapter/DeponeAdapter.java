package com.deposition.domain.adapter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.models.Snapshot;
import com.deposition.domain.models.SnapshotPointer;
import com.deposition.domain.models.valueobject.SnapshotEventLink;
import com.deposition.domain.models.valueobject.SnapshotObjectLink;
import com.deposition.domain.port.in.DeponeFileParam;
import com.deposition.domain.port.in.DeponeInPort;
import com.deposition.domain.port.in.DeponeObjectParams;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.FileStorageOutPort.FileCategory;

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
                var metadataStructures = new ArrayList<MetadataBuilder.MetadataStructure>();
                var allObjectManifestIds = new ArrayList<String>();
                var allEventIds = new ArrayList<String>();
                var allObjectManifestResources = new ArrayList<Resource>();
                var allEventResources = new ArrayList<Resource>();

                for (var fileParam : params.files()) {
                        if (fileParam == null || fileParam.resource() == null) {
                                continue;
                        }

                        var depositionFileParam = new DeponeFileParam(
                                        fileParam.resource(),
                                        fileParam.storages() == null ? List.of() : fileParam.storages());
                        var metadataStructure = MetadataBuilder.buildForFile(depositionFileParam);
                        var objectId = metadataStructure.objectId().toString();

                        var fileStorageLocation = fileStorage.persist(
                                        fileParam.resource(),
                                        FileCategory.FILE,
                                        objectId);
                        MetadataBuilder.appendStorage(metadataStructure.objectMetadata(), fileStorageLocation);

                        depositedFileObjectIds.add(metadataStructure.objectId());
                        metadataStructures.add(metadataStructure);
                }

                if (depositedFileObjectIds.isEmpty()) {
                        return;
                }

                var representationMetadataStructure = MetadataBuilder.buildForRepresentation(depositedFileObjectIds);
                metadataStructures.add(representationMetadataStructure);

                var intellectualEntityMetadataStructure = MetadataBuilder.buildForIntellectualEntity(
                                params.intellectualEntityMetadata(),
                                representationMetadataStructure.objectId());
                metadataStructures.add(intellectualEntityMetadataStructure);

                persistMetadataAndCollect(
                                metadataStructures,
                                allObjectManifestIds,
                                allEventIds,
                                allObjectManifestResources,
                                allEventResources);

                var objectRootHash = ResourceHashCalculator.calculateMerkleRootHash(allObjectManifestResources);
                var eventRootHash = ResourceHashCalculator.calculateMerkleRootHash(allEventResources);

                var snapshotManifest = buildSnapshotManifest(allObjectManifestIds, allEventIds, objectRootHash,
                                eventRootHash);
                var snapshotResource = XmlUtils.createXmlResource(snapshotManifest, "snapshot-manifest");
                fileStorage.persist(snapshotResource, FileCategory.SNAPSHOT, snapshotManifest.getId());

                var anchorRecord = buildAnchorRecord(snapshotResource);
                anchorRecord = blockchain.persistAnchorRecord(anchorRecord);
                var snapshotPointer = SnapshotPointer.builder()
                                .anchorRecordId(anchorRecord.getId())
                                // .offChainLocation() TODO: set off-chain location
                                .build();
                blockchain.persistSnapshotPoint(snapshotPointer);
        }

        private void persistMetadataAndCollect(
                        List<MetadataBuilder.MetadataStructure> metadataStructures,
                        List<String> allObjectManifestIds,
                        List<String> allEventIds,
                        List<Resource> allObjectManifestResources,
                        List<Resource> allEventResources) {
                var metadataPremis = MetadataBuilder.buildPremis(metadataStructures);
                var metadataObjectId = UUID.randomUUID().toString();
                var metadataResource = XmlUtils.createXmlResource(metadataPremis, "deposition-metadata");
                fileStorage.persist(
                                metadataResource,
                                FileCategory.OBJECT_METADATA,
                                metadataObjectId);

                for (var metadataStructure : metadataStructures) {
                        var eventId = MetadataBuilder.extractEventId(metadataStructure.creationEvent());
                        var eventResource = XmlUtils.createXmlResource(
                                        metadataStructure.creationEvent(),
                                        eventId + ".event-creation");

                        var objectId = metadataStructure.objectId().toString();
                        var objectResource = XmlUtils.createXmlResource(
                                        metadataStructure.objectMetadata(),
                                        objectId + ".object");

                        allObjectManifestIds.add(objectId);
                        allEventIds.add(eventId);
                        allObjectManifestResources.add(objectResource);
                        allEventResources.add(eventResource);
                }
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

}
