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
public class CopyrightInformation {

    private String copyrightStatus;
    private String copyrightJurisdiction;
    private LocalDate copyrightStatusDeterminationDate;
    private List<String> copyrightNote;
    private List<DocumentationIdentifier> documentationIdentifiers;
    private ApplicableDates applicableDates;
}
