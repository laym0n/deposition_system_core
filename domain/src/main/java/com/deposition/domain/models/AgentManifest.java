package com.deposition.domain.models;

import com.deposition.domain.models.valueobject.AgentEventLink;
import com.deposition.domain.models.valueobject.AgentRightsStatementLink;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AgentManifest {

    private String id;
    private Long version;
    private OffsetDateTime createdAt;
    @Default
    private List<AgentEventLink> agentEventLinks = new ArrayList<>();
    private String agentMetadataCid;
    @Default
    private List<AgentRightsStatementLink> agentRightsStatementLinks = new ArrayList<>();
}
