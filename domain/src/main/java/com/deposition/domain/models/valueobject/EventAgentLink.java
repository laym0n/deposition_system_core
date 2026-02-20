package com.deposition.domain.models.valueobject;
import com.deposition.domain.models.enums.EventAgentLinkRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAgentLink {

    private String agentId;
    private EventAgentLinkRole role;
}
