package com.deposition.domain.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
import com.deposition.domain.port.out.UserOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DepositionIndexingService {

    private final PremisSnapshotConverter premisSnapshotConverter;
    private final ObjectIndexingService objectIndexingService;
    private final UserOutPort userOutPort;

    public void indexIntellectualEntity(PremisComplexType premis,
            UUID intellectualEntityId,
            String blockchainTxId,
            String storageVersionId,
            Map<String, Object> descriptiveExtractedFields) {
        var userId = userOutPort.getCurrentUserId();
        if (userId == null) {
            return;
        }
        var snapshot = premisSnapshotConverter.map(premis);
        objectIndexingService.indexIntellectualEntity(intellectualEntityId, userId, blockchainTxId, storageVersionId,
                snapshot, descriptiveExtractedFields);
    }
}
