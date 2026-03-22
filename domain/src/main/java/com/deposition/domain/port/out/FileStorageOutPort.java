package com.deposition.domain.port.out;

import java.net.URI;
import java.util.UUID;

import org.springframework.core.io.Resource;

import com.deposition.domain.models.valueobject.Storage;

import jakarta.annotation.Nullable;

public interface FileStorageOutPort {

    Storage persist(Resource resource, String entityId);

    Resource loadPremisMetadataByObjectId(UUID objectId);

    Resource loadPremisMetadataByObjectId(UUID objectId, @Nullable String versionId);

    Resource loadByContentLocation(URI contentLocation);

}
