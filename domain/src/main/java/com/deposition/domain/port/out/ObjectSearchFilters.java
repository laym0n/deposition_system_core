package com.deposition.domain.port.out;

import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.AclRole;
import jakarta.annotation.Nullable;

import java.util.Set;

public record ObjectSearchFilters(
        @Nullable
        String principalId,

        @Nullable
        Set<AclPermission> anyAclPermissions,

        @Nullable
        Set<AclRole> anyAclRoles,

        @Nullable
        Set<Visibility> anyVisibility) {

    public enum Visibility {
        PUBLIC,
        PRIVATE
    }
}
