package com.deposition.domain.adapter;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
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

    public void validateCurrentUserOwnsObject(UUID objectId) {
        var currentUserId = resolveCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("Unauthenticated request: cannot validate object ownership");
        }

        var acl = aclOutPort.findByObjectId(objectId)
                .orElseGet(() -> buildAndPersistAclFromPremis(objectId));

        if (!acl.hasPermissionForUser(currentUserId, AclPermission.WRITE)) {
            throw new IllegalArgumentException(
                    "Related object is not accessible for current user. objectId=" + objectId);
        }
    }

    private ObjectAcl buildAndPersistAclFromPremis(UUID objectId) {
        var premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
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
