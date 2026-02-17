package com.deponic.domain.models;

import com.deponic.domain.models.valueobject.AgentEventLink;
import com.deponic.domain.models.valueobject.AgentRightsStatementLink;

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
public class AgentManifest {

    private String id;
    private Long version;
    private OffsetDateTime createdAt;
    private List<AgentEventLink> agentEventLinks;
    private String agentMetadataCid;
    private List<AgentRightsStatementLink> agentRightsStatementLinks;
}
