package com.deposition.domain.port.out;

import com.deposition.domain.models.AnchorRecord;

public interface BlockchainOutPort {

    String persistAnchorRecord(AnchorRecord anchorRecord);

    AnchorRecord loadAnchorRecord(String txId);
}
