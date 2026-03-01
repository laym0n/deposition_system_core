package com.deposition.infra.relationaldb.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deposition.infra.relationaldb.entity.ObjectAclEntity;

public interface ObjectAclJpaRepository extends JpaRepository<ObjectAclEntity, UUID> {
}
