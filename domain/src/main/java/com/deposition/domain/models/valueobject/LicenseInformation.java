package com.deposition.domain.models.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

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
