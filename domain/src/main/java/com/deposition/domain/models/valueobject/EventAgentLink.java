package com.deposition.domain.models.valueobject;

import java.util.List;

import com.deposition.domain.models.enums.EventAgentLinkRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EventAgentLink {

    private AgentIdentifier agentIdentifier;
    private List<EventAgentLinkRole> role;
}
