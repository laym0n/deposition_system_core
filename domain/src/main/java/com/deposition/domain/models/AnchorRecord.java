package com.deposition.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AnchorRecord {

    private String objectId;

    private String versionId;

    private String hash;

    private String hashAlgorithm;
}
