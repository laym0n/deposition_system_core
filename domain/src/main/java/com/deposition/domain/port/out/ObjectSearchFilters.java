package com.deposition.domain.port.out;

import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.AclRole;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record ObjectSearchFilters(
        @NotBlank
        String principalId,

        @Nullable
        Set<AclPermission> anyAclPermissions,

        @Nullable
        Set<AclRole> anyAclRoles,

        @Nullable
        Set<Visibility> anyVisibility) {

    public ObjectSearchFilters {
        if (principalId == null || principalId.isBlank()) {
            throw new IllegalArgumentException("principalId must not be blank");
        }
    }

    public enum Visibility {
        PUBLIC,
        PRIVATE
    }
}
