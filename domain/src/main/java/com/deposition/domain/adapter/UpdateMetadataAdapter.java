package com.deposition.domain.adapter;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.ObjectNotFoundException;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.UpdateMetadataInPort;
import com.deposition.domain.port.in.UpdateMetadataParams;
import com.deposition.domain.port.in.UpdateMetadataResult;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.UserService;
import com.deposition.domain.service.DepositionIndexingService;
import com.deposition.domain.service.StatisticsEventReporter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class UpdateMetadataAdapter implements UpdateMetadataInPort {

    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;
    private final PremisOwnershipValidator premisOwnershipValidator;
    private final PremisMetadataUpdater premisMetadataUpdater;
    private final DepositionIndexingService depositionIndexingService;
    private final StatisticsEventReporter statisticsEventReporter;
    private final UserService userService;

    @Override
    public UpdateMetadataResult updateMetadata(UUID objectId, UpdateMetadataParams params) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }

        premisOwnershipValidator.validateCurrentUserHasPermission(objectId, AclPermission.WRITE);

        Resource premisXml;
        try {
            premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
        } catch (IllegalArgumentException ex) {
            throw new ObjectNotFoundException(objectId);
        }

        var premis = XmlUtils.parsePremis(premisXml);

        var update = premisMetadataUpdater.applyUpdate(premis, objectId, params);
        if (!update.updated()) {
            return new UpdateMetadataResult(objectId, null);
        }

        var updatedPremisResource = XmlUtils.createXmlResource(update.premis(), "deposition-metadata");

        var premisStorage = fileStorage.persist(updatedPremisResource, objectId.toString());

        var anchorRecord = buildAnchorRecord(updatedPremisResource);
        anchorRecord = blockchain.persistAnchorRecord(anchorRecord);

        depositionIndexingService.indexIntellectualEntity(update.premis(), objectId, anchorRecord.getTxId(),
                premisStorage.getVersionId(), null);

        userService.getCurrentUserId()
                .ifPresent(userId -> statisticsEventReporter.report(
                StatisticsEventType.OBJECT_METADATA_UPDATE,
                objectId,
                null,
                userId));

        return new UpdateMetadataResult(objectId, anchorRecord.getTxId());
    }

    private static AnchorRecord buildAnchorRecord(Resource premisMetadata) {
        var premisMetadataHash = ResourceHashCalculator.sha256(premisMetadata);
        return AnchorRecord.builder()
                .premisMetadataHash(premisMetadataHash)
                .build();
    }
}
