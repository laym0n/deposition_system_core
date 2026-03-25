package com.deposition.domain.port.in.acl;

import com.deposition.domain.models.acl.AclPermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpsertObjectAclEntryRequest(
        @NotBlank
        String userId,
        @NotNull
        Set<AclPermission> permissions) {

}
