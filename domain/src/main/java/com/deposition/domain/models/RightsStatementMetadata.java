package com.deposition.domain.models;

import java.util.List;

import com.deposition.domain.models.enums.RightsBasis;
import com.deposition.domain.models.valueobject.CopyrightInformation;
import com.deposition.domain.models.valueobject.Identifier;
import com.deposition.domain.models.valueobject.LicenseInformation;
import com.deposition.domain.models.valueobject.OtherRightsInformation;
import com.deposition.domain.models.valueobject.RightsGranted;
import com.deposition.domain.models.valueobject.StatuteInformation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RightsStatementMetadata {

    private String id;
    private RightsBasis rightsBasis;
    private List<CopyrightInformation> copyrightInformation;
    private List<LicenseInformation> licenseInformation;
    private List<StatuteInformation> statuteInformation;
    private List<OtherRightsInformation> otherRightsInformation;
    private List<RightsGranted> rightsGranted;
    private List<Identifier> identifiers;
}
