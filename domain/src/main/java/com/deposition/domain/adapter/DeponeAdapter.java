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
import com.deposition.domain.port.out.FileStorageOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class DeponeAdapter implements DeponeInPort {

        private final FileStorageOutPort fileStorage;
        private final BlockchainOutPort blockchain;
        private final PremisMetadataBuilder premisMetadataBuilder;

        @Override
        public DeponeResult depone(DeponeIntellectualEntityParams params) {
                var intellectualEntityId = UUID.randomUUID();

                var persistedRepresentations = persistRepresentations(params.representations(), intellectualEntityId);

                var metadataPremis = premisMetadataBuilder.buildPremisWithEntities(
                                persistedRepresentations,
                                params.intellectualEntityMetadata(),
                                intellectualEntityId);
                var premisMetadataResource = XmlUtils.createXmlResource(metadataPremis, "deposition-metadata");
                fileStorage.persist(
                                premisMetadataResource,
                                intellectualEntityId.toString());

                var anchorRecord = buildAnchorRecord(premisMetadataResource);
                anchorRecord = blockchain.persistAnchorRecord(anchorRecord);
                return new DeponeResult(intellectualEntityId, anchorRecord.getTxId());
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
