package com.deposition.domain.port.out;

import com.deposition.domain.models.AnchorRecord;

public interface BlockchainOutPort {

    AnchorRecord persistAnchorRecord(AnchorRecord anchorRecord);

    AnchorRecord loadAnchorRecord(String txId);
}
