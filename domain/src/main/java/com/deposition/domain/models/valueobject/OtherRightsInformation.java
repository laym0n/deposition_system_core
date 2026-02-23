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
public class OtherRightsInformation {

    private List<DocumentationIdentifier> documentationIdentifiers;
    private String otherRightsBasis;
    private ApplicableDates applicableDates;
    private List<String> otherRightsNote;
}
