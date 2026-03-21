package com.deposition.domain.port.in.dto;

import java.util.List;

import com.deposition.domain.models.enums.RightsBasis;

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
            com.deposition.domain.models.valueobject.CopyrightInformation copyrightInformation,
            @Nullable
            com.deposition.domain.models.valueobject.LicenseInformation licenseInformation,
            @Nullable
            List<com.deposition.domain.models.valueobject.StatuteInformation> statuteInformation,
            @Nullable
            com.deposition.domain.models.valueobject.OtherRightsInformation otherRightsInformation,
            @Nullable
            List<com.deposition.domain.models.valueobject.RightsGranted> rightsGranted) {

    }

    /**
     * Agent participating in rights statement. If userId is set, it refers to a
     * system user, and the specified permissions should be granted via ACL in
     * OpenSearch.
     */
    public record AgentGrant(
            @Nullable
            AbstractAgent agent,
            @Nullable
            String userId,
            @Nullable
            List<String> permissions,
            @Nullable
            List<String> linkingAgentRoles) {

    }

    public record AbstractAgent(
            @NotBlank
            String id,
            @NotBlank
            String name,
            @NotNull
            com.deposition.domain.models.enums.AgentType type,
            @Nullable
            List<com.deposition.domain.models.valueobject.Identifier> identifiers) {

    }
}
