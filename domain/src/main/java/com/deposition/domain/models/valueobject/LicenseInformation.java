package com.deposition.domain.models.valueobject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseInformation {

    private List<DocumentationIdentifier> documentationIdentifiers;
    private String licenseTerms;
    private List<String> licenseNote;
    private ApplicableDates applicableDates;
}
