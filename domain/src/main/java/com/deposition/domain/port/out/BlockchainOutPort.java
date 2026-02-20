package com.deposition.domain.port.out;

import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.models.SnapshotPointer;

public interface BlockchainOutPort {

    AnchorRecord persistAnchorRecord(AnchorRecord anchorRecord);
    SnapshotPointer persistSnapshotPoint(SnapshotPointer snapshotPointer);

}
