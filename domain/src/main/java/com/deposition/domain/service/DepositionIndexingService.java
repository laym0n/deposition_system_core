package com.deposition.domain.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
import com.deposition.domain.service.acl.AclMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DepositionIndexingService {

    private final PremisSnapshotConverter premisSnapshotConverter;
    private final ObjectIndexingService objectIndexingService;

    public void indexIntellectualEntity(PremisComplexType premis,
            UUID intellectualEntityId,
            String blockchainTxId,
            String storageVersionId,
            Map<String, Object> descriptiveExtractedFields) {
        var snapshot = premisSnapshotConverter.map(premis);
        var acl = AclMapper.buildDefaultAclFromSnapshot(snapshot, intellectualEntityId);

        objectIndexingService.indexIntellectualEntity(intellectualEntityId, acl, blockchainTxId, storageVersionId,
                snapshot, descriptiveExtractedFields);
    }
}
