package com.deponic.domain.models.valueobject;
import com.deponic.domain.models.enums.EventAgentLinkRole;

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
