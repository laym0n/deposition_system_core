package com.deposition.domain.port.in.acl;

import java.util.Set;

import com.deposition.domain.models.acl.AclPermission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpsertObjectAclEntryRequest(
        @NotBlank
        String userId,
        @NotNull
        Set<AclPermission> permissions) {

}
