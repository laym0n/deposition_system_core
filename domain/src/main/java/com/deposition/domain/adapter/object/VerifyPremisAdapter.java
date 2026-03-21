package com.deposition.domain.adapter.object;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.ObjectNotFoundException;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.VerifyPremisInPort;
import com.deposition.domain.port.in.VerifyPremisResult;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.BlockchainTxLookupOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.UserOutPort;
import com.deposition.domain.service.ResourceHashCalculatorUtils;
import com.deposition.domain.service.StatisticsEventReporter;
import com.deposition.domain.service.acl.AccessValidatorService;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class VerifyPremisAdapter implements VerifyPremisInPort {

    private final FileStorageOutPort fileStorage;
    private final BlockchainTxLookupOutPort blockchainTxLookup;
    private final BlockchainOutPort blockchain;
    private final AccessValidatorService accessValidatorService;
    private final StatisticsEventReporter statisticsEventReporter;
    private final UserOutPort userService;

    @Override
    public VerifyPremisResult verifyPremis(UUID objectId, @Nullable String versionId) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }

        accessValidatorService.validateCurrentUserHasPermission(objectId, AclPermission.READ);

        Resource premisXml;
        try {
            premisXml = fileStorage.loadPremisMetadataByObjectId(objectId, versionId);
        } catch (IllegalArgumentException ex) {
            throw new ObjectNotFoundException(objectId);
        }

        var actualHash = ResourceHashCalculatorUtils.sha256(premisXml);

        var txId = blockchainTxLookup.findTxId(objectId, versionId)
                .orElseThrow(() -> new ObjectNotFoundException(objectId));

        var anchored = blockchain.loadAnchorRecord(txId);
        var expectedHash = anchored.getHash();
        if (expectedHash == null || expectedHash.isBlank()) {
            return new VerifyPremisResult(false);
        }

        userService.getOptinalCurrentUserId()
                .ifPresent(userId -> statisticsEventReporter.report(
                StatisticsEventType.PROOF_REQUEST,
                objectId,
                versionId,
                userId));

        return new VerifyPremisResult(expectedHash.equalsIgnoreCase(actualHash));
    }
}
