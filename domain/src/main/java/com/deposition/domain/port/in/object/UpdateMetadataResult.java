package com.deposition.domain.port.in.object;

import java.util.UUID;

public record UpdateMetadataResult(UUID objectId, String txId) {

}
