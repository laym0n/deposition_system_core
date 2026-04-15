package com.deposition.domain.adapter.object;

import com.deposition.domain.exception.ModuleException;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.object.UpdateMetadataInPort;
import com.deposition.domain.port.in.object.UpdateMetadataParams;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.UserOutPort;
import com.deposition.domain.service.DepositionIndexingService;
import com.deposition.domain.service.ResourceHashCalculatorUtils;
import com.deposition.domain.service.StatisticsEventReporter;
import com.deposition.domain.service.XmlUtils;
import com.deposition.domain.service.acl.AccessValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Validated
public class UpdateMetadataAdapter implements UpdateMetadataInPort {

    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;
    private final AccessValidatorService accessValidatorService;
    private final PremisMetadataUpdater premisMetadataUpdater;
    private final DepositionIndexingService depositionIndexingService;
    private final StatisticsEventReporter statisticsEventReporter;
    private final UserOutPort userService;

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

    @Override
    public DepositionResult updateMetadata(UUID objectId, UpdateMetadataParams params) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }

        accessValidatorService.validateCurrentUserHasPermission(objectId, AclPermission.WRITE);

        Resource premisXml;
        try {
            premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Object", objectId.toString());
        }

        var premis = XmlUtils.parsePremis(premisXml);

        var update = premisMetadataUpdater.applyUpdate(premis, objectId, params);
        if (!update.updated()) {
            throw new ModuleException("Not updated");
        }

        var updatedPremisResource = XmlUtils.createXmlResource(update.premis(), "deposition-metadata");

        var premisStorage = fileStorage.persist(updatedPremisResource, objectId.toString());

        var anchorRecord = buildAnchorRecord(objectId, premisStorage.getVersionId(), updatedPremisResource);
        var txId = blockchain.persistAnchorRecord(anchorRecord);

        depositionIndexingService.indexIntellectualEntityAsync(update.premis(), objectId, txId,
                premisStorage.getVersionId(), null);

        userService.getOptinalCurrentUserId()
                .ifPresent(userId -> statisticsEventReporter.report(
                        StatisticsEventType.OBJECT_METADATA_UPDATE,
                        objectId,
                        premisStorage.getVersionId(),
                        userId));

        return new DepositionResult(objectId, txId, premisStorage.getVersionId());
    }
}
