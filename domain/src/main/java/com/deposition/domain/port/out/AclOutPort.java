package com.deposition.domain.port.out;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.acl.ObjectAcl;

import java.util.Optional;
import java.util.UUID;

public interface AclOutPort {

    Optional<ObjectAcl> findByObjectId(UUID objectId);

    default ObjectAcl getByObjectId(UUID objectId) {
        return findByObjectId(objectId).orElseThrow(() -> new ResourceNotFoundException("ObjectAcl", objectId.toString()));
    }

    ObjectAcl save(ObjectAcl acl);
}
