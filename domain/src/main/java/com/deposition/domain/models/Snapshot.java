package com.deposition.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import com.deposition.domain.models.valueobject.SnapshotAgentLink;
import com.deposition.domain.models.valueobject.SnapshotEventLink;
import com.deposition.domain.models.valueobject.SnapshotObjectLink;
import com.deposition.domain.models.valueobject.SnapshotRightsStatementLink;
import com.deposition.domain.models.valueobject.SnapshotsLink;

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
