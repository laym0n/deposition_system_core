package com.deponic.domain.models;

import java.util.List;

import com.deponic.domain.models.enums.RightsBasis;
import com.deponic.domain.models.valueobject.CopyrightInformation;
import com.deponic.domain.models.valueobject.Identifier;
import com.deponic.domain.models.valueobject.LicenseInformation;
import com.deponic.domain.models.valueobject.OtherRightsInformation;
import com.deponic.domain.models.valueobject.RightsGranted;
import com.deponic.domain.models.valueobject.StatuteInformation;

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
