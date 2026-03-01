package com.deposition.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.deposition.domain.models.acl.ObjectAcl;

public interface AclOutPort {

    Optional<ObjectAcl> findByObjectId(UUID objectId);

    ObjectAcl save(ObjectAcl acl);
}
