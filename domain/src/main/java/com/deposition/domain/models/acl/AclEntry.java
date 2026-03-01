package com.deposition.domain.models.acl;

import java.util.EnumSet;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AclEntry {

    private AclPrincipal principal;

    @Default
    private EnumSet<AclPermission> permissions = EnumSet.noneOf(AclPermission.class);

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
