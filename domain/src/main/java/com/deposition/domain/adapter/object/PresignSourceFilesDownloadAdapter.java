package com.deposition.domain.adapter.object;

import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.FileMetadata;
import com.deposition.domain.models.PremisSnapshot;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.object.PresignSourceFilesDownloadInPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.PresignDownloadOutPort;
import com.deposition.domain.port.out.UserOutPort;
import com.deposition.domain.service.StatisticsEventReporter;
import com.deposition.domain.service.XmlUtils;
import com.deposition.domain.service.acl.AccessValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;
import java.util.*;

@Component
@RequiredArgsConstructor
@Validated
public class PresignSourceFilesDownloadAdapter implements PresignSourceFilesDownloadInPort {

    private static final Duration DEFAULT_PRESIGN_TTL = Duration.ofMinutes(15);

    private final FileStorageOutPort fileStorage;
    private final PresignDownloadOutPort presignDownloadOutPort;
    private final AccessValidatorService accessValidatorService;
    private final PremisSnapshotConverter premisSnapshotConverter;
    private final StatisticsEventReporter statisticsEventReporter;
    private final UserOutPort userOutPort;

    private static Map<UUID, FileMetadata> indexFiles(PremisSnapshot snapshot) {
        if (snapshot == null || snapshot.getObjects() == null) {
            return Map.of();
        }

        Map<UUID, FileMetadata> map = new HashMap<>();
        for (var obj : snapshot.getObjects()) {
            if (obj instanceof FileMetadata f && f.getId() != null) {
                map.put(f.getId(), f);
            }
        }
        return map;
    }

    private static URI resolveContentLocation(FileMetadata fileMeta, UUID fileId) {
        if (fileMeta.getStorages() == null || fileMeta.getStorages().isEmpty()) {
            throw new IllegalStateException("File has no storage locations: fileId=" + fileId);
        }
        var storage = fileMeta.getStorages().stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (storage == null || storage.getContentLocation() == null) {
            throw new IllegalStateException("File has no contentLocation: fileId=" + fileId);
        }
        return storage.getContentLocation();
    }

    @Override
    public List<PresignedSourceFile> presignSourceFilesDownload(UUID objectId, List<UUID> fileIds) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (fileIds == null || fileIds.isEmpty()) {
            throw new IllegalArgumentException("fileIds must not be empty");
        }

        accessValidatorService.validateCurrentUserHasPermission(objectId, AclPermission.READ_SOURCE_FILE);

        // Count file download intent once per request.
        userOutPort.getOptinalCurrentUserId()
                .ifPresent(userId -> statisticsEventReporter.report(
                        StatisticsEventType.FILE_DOWNLOAD,
                        objectId,
                        null,
                        userId));

        PremisSnapshot snapshot = loadSnapshot(objectId);
        Map<UUID, FileMetadata> filesById = indexFiles(snapshot);

        List<PresignedSourceFile> result = new ArrayList<>();
        for (UUID fileId : fileIds) {
            if (fileId == null) {
                continue;
            }
            FileMetadata fileMeta = filesById.get(fileId);
            if (fileMeta == null) {
                throw new ResourceNotFoundException("File", fileId.toString());
            }

            URI contentLocation = resolveContentLocation(fileMeta, fileId);
            var presigned = presignDownloadOutPort.presignGetObject(
                    new PresignDownloadOutPort.PresignGetObjectCommand(
                            contentLocation,
                            DEFAULT_PRESIGN_TTL));

            result.add(new PresignedSourceFile(
                    fileId,
                    presigned.downloadUrl(),
                    presigned.expiresAt()));
        }
        return List.copyOf(result);
    }

    private PremisSnapshot loadSnapshot(UUID objectId) {
        try {
            var premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
            PremisComplexType premis = XmlUtils.parsePremis(premisXml);
            return premisSnapshotConverter.map(premis);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Object", objectId.toString());
        }
    }

}
