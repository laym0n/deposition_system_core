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
public class RightsGranted {

    private String act;
    private List<String> restriction;
    private ApplicableDates termOfGrant;
    private ApplicableDates termOfRestriction;
    private List<String> rightsGrantedNote;
}
