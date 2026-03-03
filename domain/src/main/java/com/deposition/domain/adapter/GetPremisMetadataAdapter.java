package com.deposition.domain.adapter;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.ObjectNotFoundException;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.port.in.GetPremisMetadataInPort;
import com.deposition.domain.port.out.FileStorageOutPort;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class GetPremisMetadataAdapter implements GetPremisMetadataInPort {

    private final FileStorageOutPort fileStorage;
    private final PremisOwnershipValidator premisOwnershipValidator;

    @Override
    public Resource getPremisMetadata(UUID objectId, @Nullable String versionId) {
        // Enforce READ (validator will lazily build ACL from PREMIS if missing).
        premisOwnershipValidator.validateCurrentUserHasPermission(objectId, AclPermission.READ);

        try {
            return fileStorage.loadPremisMetadataByObjectId(objectId, versionId);
        } catch (IllegalArgumentException ex) {
            throw new ObjectNotFoundException(objectId);
        }
    }
}
