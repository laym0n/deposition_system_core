package com.deposition.domain.models;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.deposition.domain.models.valueobject.RightsStatementAgentLink;
import com.deposition.domain.models.valueobject.RightsStatementObjectLink;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RightsStatementManifest {

    private String id;
    private Long version;
    private OffsetDateTime createdAt;
    private String rightsStatementMetadataCID;
    @Default
    private List<RightsStatementAgentLink> rightsStatementAgentLinks = new ArrayList<>();
    @Default
    private List<RightsStatementObjectLink> rightsStatementObjectLinks = new ArrayList<>();
}
