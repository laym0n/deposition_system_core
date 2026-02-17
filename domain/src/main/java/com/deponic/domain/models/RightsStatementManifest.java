package com.deponic.domain.models;

import com.deponic.domain.models.valueobject.RightsStatementAgentLink;
import com.deponic.domain.models.valueobject.RightsStatementObjectLink;

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
public class RightsStatementManifest {

    private String id;
    private Long version;
    private OffsetDateTime createdAt;
    private String rightsStatementMetadataCID;
    private List<RightsStatementAgentLink> rightsStatementAgentLinks;
    private List<RightsStatementObjectLink> rightsStatementObjectLinks;
}
