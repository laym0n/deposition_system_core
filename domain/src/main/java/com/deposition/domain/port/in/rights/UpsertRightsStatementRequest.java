package com.deposition.domain.port.in.rights;

import com.deposition.domain.models.enums.AgentType;
import com.deposition.domain.models.enums.RightsBasis;
import com.deposition.domain.models.valueobject.*;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request to add/update a rights statement in PREMIS metadata.
 */
public record UpsertRightsStatementRequest(
        @Nullable
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
            String name,
            @NotNull
            AgentType type,
            @Nullable
            List<AgentIdentifier> identifiers) {

    }
}
