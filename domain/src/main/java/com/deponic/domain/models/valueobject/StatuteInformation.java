package com.deponic.domain.models.valueobject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatuteInformation {

    private String statuteJurisdiction;
    private String statuteCitation;
    private LocalDate statuteInformationDeterminationDate;
    private List<String> statuteNote;
    private List<DocumentationIdentifier> documentationIdentifiers;
    private ApplicableDates applicableDates;
}
