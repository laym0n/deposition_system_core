package com.deposition.domain.models.valueobject;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseInformation {

    private List<DocumentationIdentifier> documentationIdentifiers;
    private String licenseTerms;
    private List<String> licenseNote;
    private ApplicableDates applicableDates;
}
