package com.deposition.domain.adapter.object;

import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.FileMetadata;
import com.deposition.domain.models.PremisSnapshot;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.object.DownloadSourceFilesInPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.UserOutPort;
import com.deposition.domain.service.XmlUtils;
import com.deposition.domain.service.StatisticsEventReporter;
import com.deposition.domain.service.acl.AccessValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
@Validated
public class DownloadSourceFilesAdapter implements DownloadSourceFilesInPort {

    private final FileStorageOutPort fileStorage;
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

    private static String resolveZipEntryName(FileMetadata fileMeta,
                                              UUID fileId,
                                              Resource fileResource,
                                              Map<String, Integer> usedNames) {
        String base = fileMeta.getOriginalName();
        if (base == null || base.isBlank()) {
            base = fileResource.getFilename();
        }
        if (base == null || base.isBlank()) {
            base = fileId.toString();
        }

        String normalized = base.replace('\\', '/');
        Integer idx = usedNames.get(normalized);
        if (idx == null) {
            usedNames.put(normalized, 1);
            return normalized;
        }
        usedNames.put(normalized, idx + 1);
        int dot = normalized.lastIndexOf('.');
        if (dot > 0 && dot < normalized.length() - 1) {
            return normalized.substring(0, dot) + " (" + idx + ")" + normalized.substring(dot);
        }
        return normalized + " (" + idx + ")";
    }

    @Override
    public Resource downloadSourceFilesAsZip(UUID objectId, List<UUID> fileIds) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (fileIds == null || fileIds.isEmpty()) {
            throw new IllegalArgumentException("fileIds must not be empty");
        }

        accessValidatorService.validateCurrentUserHasPermission(objectId, AclPermission.READ_SOURCE_FILE);

        userOutPort.getOptinalCurrentUserId()
                .ifPresent(userId -> statisticsEventReporter.report(
                        StatisticsEventType.FILE_DOWNLOAD,
                        objectId,
                        null,
                        userId));

        PremisSnapshot snapshot = loadSnapshot(objectId);
        Map<UUID, FileMetadata> filesById = indexFiles(snapshot);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                Map<String, Integer> usedNames = new HashMap<>();

                for (UUID fileId : fileIds) {
                    if (fileId == null) {
                        continue;
                    }

                    FileMetadata fileMeta = filesById.get(fileId);
                    if (fileMeta == null) {
                        throw new ResourceNotFoundException("File", fileId.toString());
                    }

                    URI contentLocation = resolveContentLocation(fileMeta, fileId);
                    Resource fileResource = fileStorage.loadByContentLocation(contentLocation);

                    String entryName = resolveZipEntryName(fileMeta, fileId, fileResource, usedNames);
                    zos.putNextEntry(new ZipEntry(entryName));
                    try (var is = fileResource.getInputStream()) {
                        is.transferTo(zos);
                    }
                    zos.closeEntry();
                }
            }

            byte[] zipBytes = baos.toByteArray();
            return new ByteArrayResource(zipBytes) {
                @Override
                public String getFilename() {
                    return "source-files-" + objectId + ".zip";
                }
            };
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build source files zip for objectId=" + objectId, ex);
        }
    }

    private PremisSnapshot loadSnapshot(UUID objectId) {
        try {
            Resource premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
            PremisComplexType premis = XmlUtils.parsePremis(premisXml);
            return premisSnapshotConverter.map(premis);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Object", objectId.toString());
        }
    }
}
