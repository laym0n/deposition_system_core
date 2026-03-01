package com.deposition.domain.port.out;

import java.util.UUID;

import org.springframework.core.io.Resource;

import com.deposition.domain.models.valueobject.Storage;

public interface FileStorageOutPort {

    Storage persist(Resource resource, String entityId);

    Resource loadPremisMetadataByObjectId(UUID objectId);

}
