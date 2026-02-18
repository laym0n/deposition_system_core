package com.deponic.domain.port.in;

import java.util.List;

import com.deponic.domain.models.ObjectMetadata;

public record DeponeObjectParams(ObjectMetadata intellectualEntityMetadata, List<DeponeFileParam> files) {

}
