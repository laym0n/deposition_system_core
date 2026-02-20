package com.deposition.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import com.deposition.domain.models.valueobject.AgentEventLink;
import com.deposition.domain.models.valueobject.AgentRightsStatementLink;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentManifest {

    private String id;
    private Long version;
    private OffsetDateTime createdAt;
    private List<AgentEventLink> agentEventLinks;
    private String agentMetadataCid;
    private List<AgentRightsStatementLink> agentRightsStatementLinks;
}
