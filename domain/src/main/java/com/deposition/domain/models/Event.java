package com.deposition.domain.models;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.deposition.domain.models.enums.EventType;
import com.deposition.domain.models.valueobject.EventAgentLink;
import com.deposition.domain.models.valueobject.EventDetailInformation;
import com.deposition.domain.models.valueobject.EventObjectLink;
import com.deposition.domain.models.valueobject.EventOutcomeInformation;
import com.deposition.domain.models.valueobject.Identifier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    private UUID id;
    private EventType type;
    private OffsetDateTime dateTime;
    private List<EventOutcomeInformation> outcome;
    private List<EventDetailInformation> detail;
    private List<Identifier> identifiers;
    private List<EventObjectLink> eventObjectLinks;
    private List<EventAgentLink> eventAgentLinks;
}
