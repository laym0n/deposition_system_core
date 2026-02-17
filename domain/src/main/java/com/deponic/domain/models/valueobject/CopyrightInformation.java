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
public class CopyrightInformation {

    private String copyrightStatus;
    private String copyrightJurisdiction;
    private LocalDate copyrightStatusDeterminationDate;
    private List<String> copyrightNote;
    private List<DocumentationIdentifier> documentationIdentifiers;
    private ApplicableDates applicableDates;
}
