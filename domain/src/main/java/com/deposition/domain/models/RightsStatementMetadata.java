package com.deposition.domain.models;

import com.deposition.domain.models.valueobject.*;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

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
    private List<RightsStatementAgentLink> linkingAgentIdentifiers = new ArrayList<>();
}
