package com.deposition.domain.models.acl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectAcl {

    private UUID objectId;

    @Default
    private List<AclEntry> entries = new ArrayList<>();

    public boolean hasPermissionForUser(String userId, AclPermission permission) {
        if (userId == null || userId.isBlank() || permission == null) {
            return false;
        }

        return entries != null && entries.stream()
                .filter(Objects::nonNull)
                .anyMatch(entry -> entry.isForUser(userId) && entry.hasPermission(permission));
    }

}
