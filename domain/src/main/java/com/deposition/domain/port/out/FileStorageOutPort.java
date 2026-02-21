package com.deposition.domain.port.out;

import org.springframework.core.io.Resource;

import com.deposition.domain.models.valueobject.Storage;

public interface FileStorageOutPort {

    Storage persist(Resource resource, FileCategory category, String entityId);

    enum FileCategory {
        FILE,
        OBJECT_METADATA,
        OBJECT_MANIFEST,
        EVENT,
        SNAPSHOT
    }

}
