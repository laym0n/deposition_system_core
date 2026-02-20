package com.deposition.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import com.deposition.domain.models.valueobject.RightsStatementAgentLink;
import com.deposition.domain.models.valueobject.RightsStatementObjectLink;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RightsStatementManifest {

    private String id;
    private Long version;
    private OffsetDateTime createdAt;
    private String rightsStatementMetadataCID;
    private List<RightsStatementAgentLink> rightsStatementAgentLinks;
    private List<RightsStatementObjectLink> rightsStatementObjectLinks;
}
