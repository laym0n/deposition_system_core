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
public class RightsGranted {

    private String act;
    private List<String> restriction;
    private ApplicableDates termOfGrant;
    private ApplicableDates termOfRestriction;
    private List<String> rightsGrantedNote;
}
