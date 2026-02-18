package com.deponic.domain.in.port;

import org.springframework.core.io.Resource;

import com.deponic.domain.models.ObjectMetadata;

public record DeponeFileParam(Resource resource, ObjectMetadata metadata) {

}
