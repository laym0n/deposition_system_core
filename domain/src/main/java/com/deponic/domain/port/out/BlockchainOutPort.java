package com.deponic.domain.port.out;

import com.deponic.domain.models.AnchorRecord;
import com.deponic.domain.models.SnapshotPointer;

public interface BlockchainOutPort {

    AnchorRecord persistAnchorRecord(AnchorRecord anchorRecord);
    SnapshotPointer persistSnapshotPoint(SnapshotPointer snapshotPointer);

}
