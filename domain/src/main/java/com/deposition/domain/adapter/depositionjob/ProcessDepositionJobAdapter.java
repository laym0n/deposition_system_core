package com.deposition.domain.adapter.depositionjob;

import com.deposition.domain.adapter.builder.CommonMetadataBuilder;
import com.deposition.domain.adapter.builder.PremisMetadataBuilder;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.port.in.depositionjob.DepositionJobStatus;
import com.deposition.domain.port.in.depositionjob.ProcessDepositionJobInPort;
import com.deposition.domain.models.depositionjob.DepositionJob;
import com.deposition.domain.port.in.depositionjob.CreateDepositionJobInPort;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.DepositionJobOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.service.DepositionIndexingService;
import com.deposition.domain.service.DescriptiveMetadataService;
import com.deposition.domain.service.IntellectualEntityTypeResolver;
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
    private final IntellectualEntityTypeResolver intellectualEntityTypeResolver;

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
            CreateDepositionJobInPort.CreateDepositionJobCommand cmd = objectMapper.readValue(
                    job.requestJson(),
                    CreateDepositionJobInPort.CreateDepositionJobCommand.class);

            var entityType = intellectualEntityTypeResolver.resolveByName(cmd.intellectualEntityTypeName());

            Map<String, Object> descriptiveExtracted = descriptiveMetadataService.validateAndPersistIfPresent(
                    job.objectId(),
                    entityType,
                    cmd.descriptiveMetadata());

            var files = jobOutPort.listFiles(jobId);

            var persistedFiles = files.stream()
                    .map(f -> {
                        var attrs = fileStorage.getAttributesByContentLocation(
                                f.contentLocation(),
                                ResourceHashCalculatorUtils.DEFAULT_HASH_ALGORITHM);

                        Resource resource = fileStorage.loadByContentLocation(f.contentLocation());
                        var storage = com.deposition.domain.models.valueobject.Storage.builder()
                                .contentLocation(f.contentLocation())
                                .versionId(null)
                                .build();
                        var fileMetadataParam = new com.deposition.domain.port.in.object.FileMetadataParam(f.originalName());
                        var deponeFileParam = new com.deposition.domain.port.in.object.DeponeFileParam(fileMetadataParam, resource);
                        return new CommonMetadataBuilder.PersistedFileMetadataInput(
                                deponeFileParam,
                                storage,
                                attrs.hashAlgorithm(),
                                attrs.digestHex(),
                                attrs.sizeBytes());
                    })
                    .toList();

            var persistedReps = java.util.stream.IntStream
                    .range(0, cmd.representations().size())
                    .mapToObj(repIdx -> {
                        var repFiles = java.util.stream.IntStream
                                .range(0, files.size())
                                .filter(i -> java.util.Objects.equals(files.get(i).representationIndex(), repIdx))
                                .mapToObj(persistedFiles::get)
                                .toList();

                        return new CommonMetadataBuilder.PersistedRepresentationMetadataInput(
                                cmd.representations().get(repIdx).representationMetadata(),
                                repFiles);
                    })
                    .toList();

            var premis = premisMetadataBuilder.buildPremisWithEntities(
                    persistedReps,
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
                    entityType.name(),
                    txId,
                    premisStorage.getVersionId(),
                    descriptiveExtracted);

            var completed = new DepositionJob(
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
            var failed = new DepositionJob(
                    job.jobId(),
                    job.objectId(),
                    job.ownerUserId(),
                    DepositionJobStatus.FAILED,
                    job.requestJson(),
                    job.idempotencyKey(),
                    job.createdAt(),
                    now,
                    null,
                    null,
                    ex.getMessage());
            jobOutPort.update(failed);

            throw new IllegalStateException("Failed to process deposition jobId=" + jobId, ex);
        }
    }
}
