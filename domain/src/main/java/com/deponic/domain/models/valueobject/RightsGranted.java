package com.deponic.domain.models.valueobject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RightsGranted {

    private String act;
    private List<String> restriction;
    private ApplicableDates termOfGrant;
    private ApplicableDates termOfRestriction;
    private List<String> rightsGrantedNote;
}
