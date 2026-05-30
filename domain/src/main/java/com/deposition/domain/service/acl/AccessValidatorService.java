package com.deposition.domain.service.acl;

import com.deposition.domain.exception.ObjectAccessDeniedException;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.port.out.AclOutPort;
import com.deposition.domain.port.out.UserOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

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

        if (acl.isSuperAdmin(currentUserId)) {
            return;
        }

        if (!acl.hasPermissionForUser(currentUserId, permission)) {
            throw new ObjectAccessDeniedException(objectId);
        }
    }

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
