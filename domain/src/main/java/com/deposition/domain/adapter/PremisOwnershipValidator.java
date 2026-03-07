package com.deposition.domain.adapter;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
import com.deposition.domain.exception.ObjectAccessDeniedException;
import com.deposition.domain.exception.ObjectNotFoundException;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.port.out.AclOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
final class PremisOwnershipValidator {

    private final FileStorageOutPort fileStorage;
    private final AclOutPort aclOutPort;
    private final PremisSnapshotConverter premisSnapshotConverter;
    private final AclMapper aclMapper;

    public void validateCurrentUserHasPermission(UUID objectId, AclPermission permission) {
        var currentUserId = resolveCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("Unauthenticated request: cannot validate object permissions");
        }

        var acl = aclOutPort.findByObjectId(objectId)
                .orElseGet(() -> buildAndPersistAclFromPremis(objectId));

        if (!acl.hasPermissionForUser(currentUserId, permission)) {
            throw new ObjectAccessDeniedException(objectId);
        }
    }

    private ObjectAcl buildAndPersistAclFromPremis(UUID objectId) {
        Resource premisXml;
        try {
            premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
        } catch (IllegalArgumentException ex) {
            // Storage adapter uses IllegalArgumentException to signal missing object key.
            throw new ObjectNotFoundException(objectId);
        }
        var premis = XmlUtils.parsePremis(premisXml);

        var snapshot = premisSnapshotConverter.map(premis);
        var acl = aclMapper.buildDefaultAclFromSnapshot(snapshot, objectId);
        return aclOutPort.save(acl);
    }

    private static String resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }
}
