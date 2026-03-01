package com.deposition.infra.relationaldb.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.deposition.infra.relationaldb.entity.ObjectAclEntity;
import com.deposition.infra.relationaldb.repository.ObjectAclJpaRepository;

@Configuration
@EnableJpaRepositories(basePackageClasses = ObjectAclJpaRepository.class)
@EntityScan(basePackageClasses = ObjectAclEntity.class)
public class RelationDbInfraConfig {

}
