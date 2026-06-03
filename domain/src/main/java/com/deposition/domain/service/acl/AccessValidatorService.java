package com.deposition.domain.service.acl;

import com.deposition.domain.exception.ObjectAccessDeniedException;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.port.out.AclOutPort;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.domain.port.out.UserOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public final class AccessValidatorService {

    private final AclOutPort aclOutPort;
    private final ObjectIndexLookupOutPort objectIndexLookupOutPort;
    private final UserOutPort userOutPort;

    public void validateCurrentUserHasPermission(UUID objectId, AclPermission permission) {
        var currentUserId = userOutPort.getCurrentUserId();
        var acl = aclOutPort.getByObjectId(objectId);
        validateByAcl(objectId, permission, currentUserId, acl);
    }

    private void validateByAcl(UUID objectId, AclPermission permission, String currentUserId, ObjectAcl acl) {
        if (acl.isSuperAdmin(currentUserId)) {
            return;
        }

        if (!acl.hasPermissionForUser(currentUserId, permission)) {
            throw new ObjectAccessDeniedException(objectId);
        }
    }

    public void validateCurrentUserCanRead(UUID objectId) {
        var objectIndexDocument = objectIndexLookupOutPort.findByObjectId(objectId).get();
        try {
            var currentUserId = userOutPort.getCurrentUserId();
            validateByAcl(objectId, AclPermission.READ, currentUserId, objectIndexDocument.acl());
        } catch (RuntimeException ex) {
            if (ObjectIndexDocument.Visibility.PUBLIC.equals(objectIndexDocument.visibility())) {
                return;
            }

            if (ex instanceof ObjectAccessDeniedException || ex instanceof IllegalArgumentException) {
                throw new ObjectAccessDeniedException(objectId);
            }
            throw ex;
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
