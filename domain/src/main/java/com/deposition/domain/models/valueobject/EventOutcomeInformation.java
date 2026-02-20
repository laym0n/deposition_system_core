package com.deposition.domain.models.valueobject;
import com.deposition.domain.models.enums.EventOutcome;

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
