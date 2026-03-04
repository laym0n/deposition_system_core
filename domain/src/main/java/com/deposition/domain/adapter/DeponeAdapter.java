package com.deposition.domain.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.adapter.builder.CommonMetadataBuilder;
import com.deposition.domain.adapter.builder.PremisMetadataBuilder;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.port.in.DeponeInPort;
import com.deposition.domain.port.in.DeponeIntellectualEntityParams;
import com.deposition.domain.port.in.DeponeRepresentationParam;
import com.deposition.domain.port.in.DeponeResult;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.BlockchainTxIndexOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.service.DescriptiveMetadataService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class DeponeAdapter implements DeponeInPort {

    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;
    private final BlockchainTxIndexOutPort blockchainTxIndex;
    private final PremisMetadataBuilder premisMetadataBuilder;
    private final PremisOwnershipValidator premisOwnershipValidator;
    private final DescriptiveMetadataService descriptiveMetadataService;

    @Override
    public DeponeResult depone(DeponeIntellectualEntityParams params) {
        validateRelationshipsOwnedByCurrentUser(params);

        var intellectualEntityId = UUID.randomUUID();

        descriptiveMetadataService.validateAndPersistIfPresent(
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

        blockchainTxIndex.save(intellectualEntityId, premisStorage.getVersionId(), anchorRecord.getTxId());
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
                    // At the moment the API passes related object identifiers as LOCAL UUID strings.
                    UUID objectId;
                    try {
                        objectId = UUID.fromString(relatedObject.getValue());
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException(
                                "Invalid related object identifier (expected UUID): " + relatedObject.getValue());
                    }

                    premisOwnershipValidator.validateCurrentUserOwnsObject(objectId);
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
