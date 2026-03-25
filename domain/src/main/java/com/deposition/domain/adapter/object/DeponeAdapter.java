package com.deposition.domain.adapter.object;

import com.deposition.domain.adapter.builder.CommonMetadataBuilder;
import com.deposition.domain.adapter.builder.PremisMetadataBuilder;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.object.DeponeInPort;
import com.deposition.domain.port.in.object.DeponeIntellectualEntityParams;
import com.deposition.domain.port.in.object.DeponeRepresentationParam;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.UserOutPort;
import com.deposition.domain.service.*;
import com.deposition.domain.service.acl.AccessValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Validated
public class DeponeAdapter implements DeponeInPort {

    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;
    private final PremisMetadataBuilder premisMetadataBuilder;
    private final AccessValidatorService accessValidatorService;
    private final DescriptiveMetadataService descriptiveMetadataService;
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
    public DepositionResult depone(DeponeIntellectualEntityParams params) {
        validateRelationshipsOwnedByCurrentUser(params);

        var intellectualEntityId = UUID.randomUUID();

        var descriptiveExtracted = descriptiveMetadataService.validateAndPersistIfPresent(
                intellectualEntityId,
                params.intellectualEntityType(),
                params.descriptiveMetadata());

        var persistedRepresentations = persistRepresentations(params.representations(), intellectualEntityId);

        var metadataPremis = premisMetadataBuilder.buildPremisWithEntities(
                persistedRepresentations,
                params.intellectualEntityMetadata(),
                intellectualEntityId);

        var premisMetadataResource = XmlUtils.createXmlResource(metadataPremis, "deposition-metadata");
        var premisStorage = fileStorage.persist(premisMetadataResource, intellectualEntityId.toString());

        var anchorRecord = buildAnchorRecord(intellectualEntityId, premisStorage.getVersionId(), premisMetadataResource);
        var txId = blockchain.persistAnchorRecord(anchorRecord);

        depositionIndexingService.indexIntellectualEntity(metadataPremis, intellectualEntityId, txId,
                premisStorage.getVersionId(), descriptiveExtracted);

        userService.getOptinalCurrentUserId()
                .ifPresent(userId -> statisticsEventReporter.report(
                        StatisticsEventType.OBJECT_DEPOSIT,
                        intellectualEntityId,
                        null,
                        userId));

        return new DepositionResult(intellectualEntityId, txId, premisStorage.getVersionId());
    }

    private void validateRelationshipsOwnedByCurrentUser(DeponeIntellectualEntityParams params) {
        if (params == null || params.intellectualEntityMetadata() == null
                || params.intellectualEntityMetadata().relationships() == null) {
            return;
        }

        params.intellectualEntityMetadata().relationships().stream()
                .filter(relationship -> relationship != null && relationship.getRelatedObjects() != null)
                .flatMap(relationship -> relationship.getRelatedObjects().stream())
                .filter(relatedObject -> relatedObject != null && relatedObject.getValue() != null
                        && !relatedObject.getValue().isBlank())
                .forEach(relatedObject -> {
                    UUID objectId;
                    try {
                        objectId = UUID.fromString(relatedObject.getValue());
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException(
                                "Invalid related object identifier (expected UUID): " + relatedObject.getValue());
                    }

                    accessValidatorService.validateCurrentUserHasPermission(objectId, AclPermission.WRITE);
                });
    }

    private List<CommonMetadataBuilder.PersistedRepresentationMetadataInput> persistRepresentations(
            List<DeponeRepresentationParam> representations, UUID intellectualEntityId) {
        return representations.stream()
                .map(representation -> {
                    var persistedFiles = representation.fileParams().stream()
                            .map(fileParam -> {
                                var fileResource = fileParam.resource();
                                var storage = fileStorage.persist(fileResource,
                                        intellectualEntityId.toString());
                                return new CommonMetadataBuilder.PersistedFileMetadataInput(
                                        fileParam, storage);
                            }).toList();
                    return new CommonMetadataBuilder.PersistedRepresentationMetadataInput(
                            representation.representationMetadata(),
                            persistedFiles);
                })
                .toList();
    }

}
