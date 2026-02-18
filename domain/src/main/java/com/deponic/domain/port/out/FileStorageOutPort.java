package com.deponic.domain.port.out;

import org.springframework.core.io.Resource;

import com.deponic.domain.models.valueobject.Storage;

public interface FileStorageOutPort {

    Storage persist(Resource resource);

}
