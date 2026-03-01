package com.deposition.infra.relationaldb.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.port.out.AclOutPort;
import com.deposition.infra.relationaldb.mapper.AclMapper;
import com.deposition.infra.relationaldb.repository.ObjectAclJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaAclAdapter implements AclOutPort {

    private final ObjectAclJpaRepository repository;
    private final AclMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<ObjectAcl> findByObjectId(UUID objectId) {
        return repository.findById(objectId).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public ObjectAcl save(ObjectAcl acl) {
        if (acl == null || acl.getObjectId() == null) {
            throw new IllegalArgumentException("ACL or objectId must not be null");
        }

        var entity = mapper.toEntity(acl);
        repository.saveAndFlush(entity);
        return acl;
    }
}
