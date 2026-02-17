package com.deponic.domain.models;

import java.time.OffsetDateTime;
import java.util.List;

import com.deponic.domain.models.enums.EventType;
import com.deponic.domain.models.valueobject.EventAgentLink;
import com.deponic.domain.models.valueobject.EventDetailInformation;
import com.deponic.domain.models.valueobject.EventObjectLink;
import com.deponic.domain.models.valueobject.EventOutcomeInformation;
import com.deponic.domain.models.valueobject.Identifier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    private String id;
    private EventType type;
    private OffsetDateTime dateTime;
    private List<EventOutcomeInformation> outcome;
    private List<EventDetailInformation> detail;
    private List<Identifier> identifiers;
    private List<EventObjectLink> eventObjectLinks;
    private List<EventAgentLink> eventAgentLinks;
}
