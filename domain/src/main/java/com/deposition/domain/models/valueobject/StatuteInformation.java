package com.deposition.domain.models.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Data
@SuperBuilder
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
