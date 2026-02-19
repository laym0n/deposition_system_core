package com.deponic.domain.models;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnchorRecord {

    private String id;
    private String snapshotHash;
    private OffsetDateTime timestamp;
}
