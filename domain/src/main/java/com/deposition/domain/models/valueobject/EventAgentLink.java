package com.deposition.domain.models.valueobject;

import com.deposition.domain.models.enums.EventAgentLinkRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EventAgentLink {

    private AgentIdentifier agentIdentifier;
    private List<EventAgentLinkRole> role;
}
