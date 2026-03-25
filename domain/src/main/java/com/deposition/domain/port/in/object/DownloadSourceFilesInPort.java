package com.deposition.domain.port.in.object;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Validated
public interface DownloadSourceFilesInPort {

    Resource downloadSourceFilesAsZip(@NotNull UUID objectId, @NotEmpty List<UUID> fileIds);
}
