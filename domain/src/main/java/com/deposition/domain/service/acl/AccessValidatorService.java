package com.deposition.domain.service.acl;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.deposition.domain.exception.ObjectAccessDeniedException;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.port.out.AclOutPort;
import com.deposition.domain.port.out.UserOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class AccessValidatorService {

    private final AclOutPort aclOutPort;
    private final UserOutPort userOutPort;

    public void validateCurrentUserHasPermission(UUID objectId, AclPermission permission) {
        var currentUserId = userOutPort.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("Unauthenticated request: cannot validate object permissions");
        }

        var acl = aclOutPort.getByObjectId(objectId);

        // Object-scoped SUPER_ADMIN bypasses permissions.
        if (acl.isSuperAdmin(currentUserId)) {
            return;
        }

        if (!acl.hasPermissionForUser(currentUserId, permission)) {
            throw new ObjectAccessDeniedException(objectId);
        }
    }

    /**
     * Validates that current user is SUPER_ADMIN for given object.
     */
    public void validateCurrentUserIsSuperAdmin(UUID objectId) {
        var currentUserId = userOutPort.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("Unauthenticated request: cannot validate object permissions");
        }

        var acl = aclOutPort.getByObjectId(objectId);
        if (!acl.isSuperAdmin(currentUserId)) {
            throw new ObjectAccessDeniedException(objectId);
        }
    }

}
