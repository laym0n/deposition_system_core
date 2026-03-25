package com.deposition.domain.models.acl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.EnumSet;
import java.util.Objects;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AclEntry {

    private AclPrincipal principal;

    @Default
    private AclRole role = AclRole.USER;

    @Default
    private EnumSet<AclPermission> permissions = EnumSet.noneOf(AclPermission.class);

    @JsonIgnore
    public boolean isSuperAdminForObject() {
        return role == AclRole.SUPER_ADMIN;
    }

    public boolean hasPermission(AclPermission permission) {
        if (permission == null) {
            return false;
        }
        return permissions != null && permissions.contains(permission);
    }

    public boolean isForUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return principal != null
                && principal.getType() == AclPrincipalType.USER
                && Objects.equals(principal.getId(), userId);
    }
}
