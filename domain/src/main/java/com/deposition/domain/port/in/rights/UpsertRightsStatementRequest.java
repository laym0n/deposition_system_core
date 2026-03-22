package com.deposition.domain.port.in.rights;

import java.util.List;

import com.deposition.domain.models.enums.AgentType;
import com.deposition.domain.models.enums.RightsBasis;
import com.deposition.domain.models.valueobject.CopyrightInformation;
import com.deposition.domain.models.valueobject.Identifier;
import com.deposition.domain.models.valueobject.LicenseInformation;
import com.deposition.domain.models.valueobject.OtherRightsInformation;
import com.deposition.domain.models.valueobject.RightsGranted;
import com.deposition.domain.models.valueobject.StatuteInformation;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request to add/update a rights statement in PREMIS metadata.
 */
public record UpsertRightsStatementRequest(
        @NotBlank
        String rightsStatementId,
        @NotNull
        RightsBasis rightsBasis,
        @Nullable
        RightsStatementPayload payload,
        @Nullable
        List<AgentGrant> agents) {

    /**
     * Mirrors fields of PREMIS rightsStatement.
     */
    public record RightsStatementPayload(
            @Nullable
            CopyrightInformation copyrightInformation,
            @Nullable
            LicenseInformation licenseInformation,
            @Nullable
            List<StatuteInformation> statuteInformation,
            @Nullable
            OtherRightsInformation otherRightsInformation,
            @Nullable
            List<RightsGranted> rightsGranted) {

    }

    public record AgentGrant(
            @NotNull
            AgentDto agent,
            @Nullable
            List<String> linkingAgentRoles) {

    }

    public record AgentDto(
            @NotBlank
            String id,
            @NotBlank
            String name,
            @NotNull
            AgentType type,
            @Nullable
            List<Identifier> identifiers) {

    }
}
