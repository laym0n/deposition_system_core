package com.deposition.domain.adapter.object;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.adapter.builder.CommonMetadataBuilder;
import com.deposition.domain.adapter.builder.PremisMetadataBuilder;
import com.deposition.domain.adapter.acl.PremisOwnershipValidator;
import com.deposition.domain.adapter.common.ResourceHashCalculator;
import com.deposition.domain.adapter.common.XmlUtils;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.DeponeInPort;
import com.deposition.domain.port.in.DeponeIntellectualEntityParams;
import com.deposition.domain.port.in.DeponeRepresentationParam;
import com.deposition.domain.port.in.DeponeResult;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.UserService;
import com.deposition.domain.service.DepositionIndexingService;
import com.deposition.domain.service.DescriptiveMetadataService;
import com.deposition.domain.service.StatisticsEventReporter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class DeponeAdapter implements DeponeInPort {

    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;
    private final PremisMetadataBuilder premisMetadataBuilder;
    private final PremisOwnershipValidator premisOwnershipValidator;
    private final DescriptiveMetadataService descriptiveMetadataService;
    private final DepositionIndexingService depositionIndexingService;
    private final StatisticsEventReporter statisticsEventReporter;
    private final UserService userService;

    @Override
    public DeponeResult depone(DeponeIntellectualEntityParams params) {
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

        var anchorRecord = buildAnchorRecord(premisMetadataResource);
        anchorRecord = blockchain.persistAnchorRecord(anchorRecord);

        depositionIndexingService.indexIntellectualEntity(metadataPremis, intellectualEntityId, anchorRecord.getTxId(),
                premisStorage.getVersionId(), descriptiveExtracted);

        userService.getCurrentUserId()
                .ifPresent(userId -> statisticsEventReporter.report(
                StatisticsEventType.OBJECT_DEPOSIT,
                intellectualEntityId,
                null,
                userId));

        return new DeponeResult(intellectualEntityId, anchorRecord.getTxId());
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

                    premisOwnershipValidator.validateCurrentUserHasPermission(objectId, AclPermission.WRITE);
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

    private AnchorRecord buildAnchorRecord(Resource premisMetadata) {
        var premisMetadataHash = ResourceHashCalculator.sha256(premisMetadata);

        return AnchorRecord.builder()
                .premisMetadataHash(premisMetadataHash)
                .build();
    }

}
