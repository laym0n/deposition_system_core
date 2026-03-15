package com.deposition.domain.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;

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
        var userId = resolveCurrentUserId();
        if (userId == null) {
            return;
        }
        var snapshot = premisSnapshotConverter.map(premis);
        objectIndexingService.indexIntellectualEntity(intellectualEntityId, userId, blockchainTxId, storageVersionId,
                snapshot, descriptiveExtractedFields);
    }

    private static String resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }
}
