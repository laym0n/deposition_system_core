package com.deposition.domain.port.in;

import java.util.List;

import com.deposition.domain.models.ObjectMetadata;

public record DeponeObjectParams(ObjectMetadata intellectualEntityMetadata, List<DeponeFileParam> files) {

}
