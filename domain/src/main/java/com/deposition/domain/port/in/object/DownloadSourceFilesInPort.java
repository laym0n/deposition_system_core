package com.deposition.domain.port.in.object;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Validated
public interface DownloadSourceFilesInPort {

    Resource downloadSourceFilesAsZip(@NotNull UUID objectId, @NotEmpty List<UUID> fileIds);
}
