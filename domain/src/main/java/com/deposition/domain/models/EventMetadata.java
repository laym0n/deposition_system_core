package com.deposition.domain.models;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.deposition.domain.models.enums.EventType;
import com.deposition.domain.models.valueobject.EventAgentLink;
import com.deposition.domain.models.valueobject.EventDetailInformation;
import com.deposition.domain.models.valueobject.EventIdentifier;
import com.deposition.domain.models.valueobject.EventObjectLink;
import com.deposition.domain.models.valueobject.EventOutcomeInformation;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EventMetadata {

    private UUID id;
    private EventType type;
    private OffsetDateTime dateTime;
    @Default
    private List<EventOutcomeInformation> outcome = new ArrayList<>();
    @Default
    private List<EventDetailInformation> detail = new ArrayList<>();
    private EventIdentifier identifier;
    @Default
    private List<EventObjectLink> objectLinks = new ArrayList<>();
    @Default
    private List<EventAgentLink> agentLinks = new ArrayList<>();
}
