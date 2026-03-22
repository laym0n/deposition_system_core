package com.deposition.domain.service;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.domain.port.out.ObjectIndexOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PremisPersistenceService {

    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;
    private final DepositionIndexingService depositionIndexingService;

    public DepositionResult persistPremis(UUID objectId,
            PremisComplexType premis) {
        var premisResource = XmlUtils.createXmlResource(premis, "deposition-metadata");
        var premisStorage = fileStorage.persist(premisResource, objectId.toString());

        var anchorRecord = buildAnchorRecord(objectId, premisStorage.getVersionId(), premisResource);
        var txId = blockchain.persistAnchorRecord(anchorRecord);

        depositionIndexingService.updatePremisAndAnchors(objectId, premis, txId, premisStorage.getVersionId());

        return new DepositionResult(objectId, txId, premisStorage.getVersionId());
    }

    private static AnchorRecord buildAnchorRecord(UUID objectId, String versionId, Resource premisMetadata) {
        String algorithm = ResourceHashCalculatorUtils.DEFAULT_HASH_ALGORITHM;
        var premisMetadataHash = ResourceHashCalculatorUtils.calculateHash(premisMetadata, algorithm);
        return AnchorRecord.builder()
                .objectId(objectId.toString())
                .versionId(versionId)
                .hash(premisMetadataHash)
                .hashAlgorithm(algorithm)
                .build();
    }
}
