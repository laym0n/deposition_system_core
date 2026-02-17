package com.deponic.domain.models;

import com.deponic.domain.models.valueobject.SnapshotAgentLink;
import com.deponic.domain.models.valueobject.SnapshotEventLink;
import com.deponic.domain.models.valueobject.SnapshotObjectLink;
import com.deponic.domain.models.valueobject.SnapshotRightsStatementLink;
import com.deponic.domain.models.valueobject.SnapshotsLink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {

    private String id;
    private OffsetDateTime createdAt;
    private String stateHash;
    private String blockchainTxId;
    private List<SnapshotObjectLink> objectLinks;
    private String objectRootHash;
    private List<SnapshotEventLink> eventLinks;
    private String eventRootHash;
    private List<SnapshotAgentLink> agentLinks;
    private String agentRootHash;
    private List<SnapshotRightsStatementLink> rightsStatementLinks;
    private String rightsRootHash;
    private List<SnapshotsLink> snapshotsLinks;
    private String snapshotsRootHash;
}
