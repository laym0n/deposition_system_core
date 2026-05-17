package com.deposition.infra.relationaldb.adapter;

import com.deposition.domain.models.depositionjob.DepositionJob;
import com.deposition.domain.models.depositionjob.DepositionJobFile;
import com.deposition.domain.port.in.depositionjob.DepositionJobStatus;
import com.deposition.domain.port.out.DepositionJobOutPort;
import com.deposition.infra.relationaldb.entity.DepositionJobEntity;
import com.deposition.infra.relationaldb.entity.DepositionJobFileEntity;
import com.deposition.infra.relationaldb.repository.DepositionJobFileJpaRepository;
import com.deposition.infra.relationaldb.repository.DepositionJobJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaDepositionJobAdapter implements DepositionJobOutPort {

    private final DepositionJobJpaRepository jobRepository;
    private final DepositionJobFileJpaRepository fileRepository;

    @Override
    public DepositionJob create(DepositionJob job) {
        var entity = toEntity(job);
        var saved = jobRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public DepositionJob update(DepositionJob job) {
        var entity = toEntity(job);
        var saved = jobRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<DepositionJob> findById(UUID jobId) {
        return jobRepository.findById(jobId).map(JpaDepositionJobAdapter::toDomain);
    }

    @Override
    public Optional<DepositionJob> findByOwnerAndIdempotencyKey(String ownerUserId, String idempotencyKey) {
        return jobRepository.findByOwnerUserIdAndIdempotencyKey(ownerUserId, idempotencyKey)
                .map(JpaDepositionJobAdapter::toDomain);
    }

    @Override
    public DepositionJobPage listByOwnerUserId(String ownerUserId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = jobRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId, pageable);
        var items = result.getContent().stream().map(JpaDepositionJobAdapter::toDomain).toList();
        return new DepositionJobPage(items, result.getTotalElements());
    }

    @Override
    public List<DepositionJobFile> listFiles(UUID jobId) {
        return fileRepository.findAllByJobId(jobId).stream().map(JpaDepositionJobAdapter::toDomain).toList();
    }

    @Override
    public void upsertFiles(UUID jobId, List<DepositionJobFile> files) {
        if (files == null) {
            return;
        }
        var entities = files.stream().map(JpaDepositionJobAdapter::toEntity).toList();
        fileRepository.saveAll(entities);
    }

    private static DepositionJobEntity toEntity(DepositionJob job) {
        return DepositionJobEntity.builder()
                .jobId(job.jobId())
                .objectId(job.objectId())
                .ownerUserId(job.ownerUserId())
                .status(job.status().name())
                .requestJson(job.requestJson())
                .idempotencyKey(job.idempotencyKey())
                .createdAt(job.createdAt())
                .updatedAt(job.updatedAt())
                .resultTxId(job.resultTxId())
                .resultVersionId(job.resultVersionId())
                .errorMessage(job.errorMessage())
                .build();
    }

    private static DepositionJob toDomain(DepositionJobEntity entity) {
        return new DepositionJob(
                entity.getJobId(),
                entity.getObjectId(),
                entity.getOwnerUserId(),
                DepositionJobStatus.valueOf(entity.getStatus()),
                entity.getRequestJson(),
                entity.getIdempotencyKey(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getResultTxId(),
                entity.getResultVersionId(),
                entity.getErrorMessage()
        );
    }

    private static DepositionJobFileEntity toEntity(DepositionJobFile f) {
        return DepositionJobFileEntity.builder()
                .fileId(f.fileId())
                .jobId(f.jobId())
                .representationIndex(f.representationIndex())
                .originalName(f.originalName())
                .contentType(f.contentType())
                .sizeBytesExpected(f.sizeBytesExpected())
                .objectKey(f.objectKey())
                .contentLocation(f.contentLocation() == null ? null : f.contentLocation().toString())
                .build();
    }

    private static DepositionJobFile toDomain(DepositionJobFileEntity entity) {
        return new DepositionJobFile(
                entity.getFileId(),
                entity.getJobId(),
                entity.getRepresentationIndex(),
                entity.getOriginalName(),
                entity.getContentType(),
                entity.getSizeBytesExpected(),
                entity.getObjectKey(),
                entity.getContentLocation() == null ? null : java.net.URI.create(entity.getContentLocation())
        );
    }
}
