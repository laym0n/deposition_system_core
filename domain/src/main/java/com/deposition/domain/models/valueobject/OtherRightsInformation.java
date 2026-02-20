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
public class OtherRightsInformation {

    private List<DocumentationIdentifier> documentationIdentifiers;
    private String otherRightsBasis;
    private ApplicableDates applicableDates;
    private List<String> otherRightsNote;
}
