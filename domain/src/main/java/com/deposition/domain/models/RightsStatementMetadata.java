package com.deposition.domain.models;

import java.util.ArrayList;
import java.util.List;

import com.deposition.domain.models.valueobject.CopyrightInformation;
import com.deposition.domain.models.valueobject.Identifier;
import com.deposition.domain.models.valueobject.LicenseInformation;
import com.deposition.domain.models.valueobject.OtherRightsInformation;
import com.deposition.domain.models.valueobject.RightsGranted;
import com.deposition.domain.models.valueobject.StatuteInformation;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RightsStatementMetadata {

    private String id;
    private String rightsBasis;
    @Default
    private List<CopyrightInformation> copyrightInformation = new ArrayList<>();
    @Default
    private List<LicenseInformation> licenseInformation = new ArrayList<>();
    @Default
    private List<StatuteInformation> statuteInformation = new ArrayList<>();
    private OtherRightsInformation otherRightsInformation;
    @Default
    private List<RightsGranted> rightsGranted = new ArrayList<>();
    @Default
    private List<Identifier> identifiers = new ArrayList<>();
}
