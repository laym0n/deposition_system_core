package com.deponic.domain.models.valueobject;
import com.deponic.domain.models.enums.EventOutcome;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventOutcomeInformation {

    private EventOutcome outcome;
    private EventOutcomeInformationDetail outcomeDetail;
}
