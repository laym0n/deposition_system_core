package com.deposition.domain.port.in.object;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Validated
public interface PresignSourceFilesDownloadInPort {

    List<PresignedSourceFile> presignSourceFilesDownload(
            @NotNull UUID objectId,
            @NotEmpty List<UUID> fileIds);

    record PresignedSourceFile(
            @NotNull UUID fileId,
            @NotNull URI downloadUrl,
            @NotNull OffsetDateTime expiresAt) {
    }
}
