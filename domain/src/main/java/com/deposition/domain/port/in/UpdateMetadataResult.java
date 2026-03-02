package com.deposition.domain.port.in;

import java.util.UUID;

public record UpdateMetadataResult(UUID objectId, String txId) {

}
