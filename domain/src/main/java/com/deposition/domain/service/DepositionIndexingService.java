package com.deposition.domain.service;

import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.domain.service.acl.AclMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DepositionIndexingService {

    private final PremisSnapshotConverter premisSnapshotConverter;
    private final ObjectIndexingService objectIndexingService;
    private final ObjectIndexLookupOutPort objectIndexLookupOutPort;

    private static List<ObjectIndexDocument.Anchor> mergeAnchors(
            List<ObjectIndexDocument.Anchor> existing,
            String storageVersionId,
            String blockchainTxId) {
        boolean hasNew = (storageVersionId != null && !storageVersionId.isBlank())
                || (blockchainTxId != null && !blockchainTxId.isBlank());

        if (!hasNew) {
            return existing;
        }

        var next = new ObjectIndexDocument.Anchor(storageVersionId, blockchainTxId, ZonedDateTime.now());
        var result = new ArrayList<ObjectIndexDocument.Anchor>();
        result.add(next);

        if (existing != null) {
            for (var a : existing) {
                if (a == null) {
                    continue;
                }
                if (Objects.equals(a.storageVersionId(), next.storageVersionId())
                        && Objects.equals(a.blockchainTxId(), next.blockchainTxId())) {
                    continue;
                }
                result.add(a);
            }
        }
        return List.copyOf(result);
    }

    public void indexIntellectualEntity(PremisComplexType premis,
                                        UUID intellectualEntityId,
                                         String intellectualEntityTypeName,
                                        String blockchainTxId,
                                        String storageVersionId,
                                        Map<String, Object> descriptiveExtractedFields) {
        if (intellectualEntityTypeName == null || intellectualEntityTypeName.isBlank()) {
            intellectualEntityTypeName = objectIndexLookupOutPort.findByObjectId(intellectualEntityId)
                    .map(ObjectIndexDocument::intellectualEntityTypeName)
                    .orElseThrow(() -> new ResourceNotFoundException("Object", intellectualEntityId.toString()));
        }

        var snapshot = premisSnapshotConverter.map(premis);
        var acl = AclMapper.buildDefaultAclFromSnapshot(snapshot, intellectualEntityId);

        List<ObjectIndexDocument.Anchor> anchors = null;
        if ((storageVersionId != null && !storageVersionId.isBlank())
                || (blockchainTxId != null && !blockchainTxId.isBlank())) {
            anchors = List.of(new ObjectIndexDocument.Anchor(storageVersionId, blockchainTxId, null));
        }

        objectIndexingService.indexIntellectualEntity(
                intellectualEntityId,
                intellectualEntityTypeName,
                acl,
                anchors,
                snapshot,
                descriptiveExtractedFields);
    }

    @Async("depositionIndexingExecutor")
    public void indexIntellectualEntityAsync(PremisComplexType premis,
                                             UUID intellectualEntityId,
                                              String intellectualEntityTypeName,
                                             String blockchainTxId,
                                             String storageVersionId,
                                             Map<String, Object> descriptiveExtractedFields) {
        indexIntellectualEntity(premis,
                intellectualEntityId,
                intellectualEntityTypeName,
                blockchainTxId,
                storageVersionId,
                descriptiveExtractedFields);
    }

    public void updatePremisAndAnchors(UUID objectId,
                                       PremisComplexType premis,
                                       String blockchainTxId,
                                       String storageVersionId) {

        var existing = objectIndexLookupOutPort.findByObjectId(objectId)
                .orElseThrow(() -> new ResourceNotFoundException("Object", objectId.toString()));

        var snapshot = premisSnapshotConverter.map(premis);

        List<ObjectIndexDocument.Anchor> updatedAnchors = mergeAnchors(existing.anchors(), storageVersionId, blockchainTxId);

        objectIndexingService.indexIntellectualEntity(
                objectId,
                existing.intellectualEntityTypeName(),
                existing.acl(),
                updatedAnchors,
                snapshot,
                existing.descriptive());
    }

    public void updatePremisAnchorsAndAcl(UUID objectId,
                                          PremisComplexType premis,
                                          String blockchainTxId,
                                          String storageVersionId,
                                          ObjectAcl acl) {

        var existing = objectIndexLookupOutPort.findByObjectId(objectId)
                .orElseThrow(() -> new ResourceNotFoundException("Object", objectId.toString()));

        var snapshot = premisSnapshotConverter.map(premis);

        List<ObjectIndexDocument.Anchor> updatedAnchors = mergeAnchors(existing.anchors(), storageVersionId, blockchainTxId);

        ObjectAcl resolvedAcl = acl == null ? existing.acl() : acl;

        objectIndexingService.indexIntellectualEntity(
                objectId,
                existing.intellectualEntityTypeName(),
                resolvedAcl,
                updatedAnchors,
                snapshot,
                existing.descriptive());
    }
}
