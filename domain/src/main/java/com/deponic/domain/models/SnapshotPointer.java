package com.deponic.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotPointer {

    private String id;
    private String anchorRecordId;
    private String offChainLocation;
}
