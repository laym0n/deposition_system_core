package com.deposition.domain.port.in;

import java.util.List;

import org.springframework.core.io.Resource;

import com.deposition.domain.models.valueobject.Storage;

public record DeponeFileParam(Resource resource, List<Storage> storages) {

}
