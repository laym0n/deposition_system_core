package com.deposition.domain.adapter.depositionjob;

import com.deposition.domain.adapter.builder.CommonMetadataBuilder;
import com.deposition.domain.adapter.builder.PremisMetadataBuilder;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.port.in.depositionjob.DepositionJobStatus;
import com.deposition.domain.port.in.depositionjob.ProcessDepositionJobInPort;
import com.deposition.domain.port.in.depositionjob.CreateDepositionJobInPort;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.DepositionJobOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.service.DepositionIndexingService;
import com.deposition.domain.service.DescriptiveMetadataService;
import com.deposition.domain.service.ResourceHashCalculatorUtils;
import com.deposition.domain.service.XmlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Validated
@RequiredArgsConstructor
public class ProcessDepositionJobAdapter implements ProcessDepositionJobInPort {

    private final DepositionJobOutPort jobOutPort;
    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;
    private final PremisMetadataBuilder premisMetadataBuilder;
    private final DescriptiveMetadataService descriptiveMetadataService;
    private final DepositionIndexingService depositionIndexingService;
    private final ObjectMapper objectMapper;

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
    public void process(UUID jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("jobId must not be null");
        }

        var job = jobOutPort.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("DepositionJob", jobId.toString()));
        if (job.status() != DepositionJobStatus.PROCESSING) {
            return;
        }

        var now = OffsetDateTime.now(ZoneOffset.UTC);

        try {
            // Reconstruct request (only the fields needed for building PREMIS).
            CreateDepositionJobInPort.CreateDepositionJobCommand cmd = objectMapper.readValue(
                    job.requestJson(),
                    CreateDepositionJobInPort.CreateDepositionJobCommand.class);

            Map<String, Object> descriptiveExtracted = descriptiveMetadataService.validateAndPersistIfPresent(
                    job.objectId(),
                    cmd.intellectualEntityType(),
                    cmd.descriptiveMetadata());

            var files = jobOutPort.listFiles(jobId);
            var persistedFiles = files.stream()
                    .map(f -> {
                        Resource resource = fileStorage.loadByContentLocation(f.contentLocation());
                        String hashHex = ResourceHashCalculatorUtils.calculateHash(resource, ResourceHashCalculatorUtils.DEFAULT_HASH_ALGORITHM);
                        long size;
                        try {
                            size = resource.contentLength();
                        } catch (Exception ex) {
                            size = -1;
                        }
                        var storage = com.deposition.domain.models.valueobject.Storage.builder()
                                .contentLocation(f.contentLocation())
                                .versionId(null)
                                .build();
                        var fileMetadataParam = new com.deposition.domain.port.in.object.FileMetadataParam(f.originalName());
                        var deponeFileParam = new com.deposition.domain.port.in.object.DeponeFileParam(fileMetadataParam, resource);
                        return new CommonMetadataBuilder.PersistedFileMetadataInput(
                                deponeFileParam,
                                storage,
                                ResourceHashCalculatorUtils.DEFAULT_HASH_ALGORITHM,
                                hashHex,
                                size);
                    })
                    .toList();

            var repMeta = cmd.representationMetadata();
            var persistedRep = new CommonMetadataBuilder.PersistedRepresentationMetadataInput(repMeta, persistedFiles);

            var premis = premisMetadataBuilder.buildPremisWithEntities(
                    List.of(persistedRep),
                    cmd.intellectualEntityMetadata(),
                    job.objectId(),
                    job.ownerUserId());

            var premisResource = XmlUtils.createXmlResource(premis, "deposition-metadata");
            var premisStorage = fileStorage.persist(premisResource, job.objectId().toString());

            var anchorRecord = buildAnchorRecord(job.objectId(), premisStorage.getVersionId(), premisResource);
            var txId = blockchain.persistAnchorRecord(anchorRecord);

            depositionIndexingService.indexIntellectualEntityAsync(
                    premis,
                    job.objectId(),
                    txId,
                    premisStorage.getVersionId(),
                    descriptiveExtracted);

            var completed = new DepositionJobOutPort.DepositionJob(
                    job.jobId(),
                    job.objectId(),
                    job.ownerUserId(),
                    DepositionJobStatus.COMPLETED,
                    job.requestJson(),
                    job.idempotencyKey(),
                    job.createdAt(),
                    now,
                    txId,
                    premisStorage.getVersionId(),
                    null);
            jobOutPort.update(completed);
        } catch (Exception ex) {
            var failed = new DepositionJobOutPort.DepositionJob(
                    job.jobId(),
                    job.objectId(),
                    job.ownerUserId(),
                    DepositionJobStatus.FAILED,
                    job.requestJson(),
                    job.idempotencyKey(),
                    job.createdAt(),
                    now,
                    job.resultTxId(),
                    job.resultVersionId(),
                    ex.getMessage());
            jobOutPort.update(failed);
        }
    }
}
