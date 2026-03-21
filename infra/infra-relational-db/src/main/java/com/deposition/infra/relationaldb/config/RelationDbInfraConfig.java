package com.deposition.infra.relationaldb.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.deposition.infra.relationaldb.entity.EntityScanMarker;
import com.deposition.infra.relationaldb.repository.RepositoryScanMarker;

@Configuration
@EnableJpaRepositories(basePackageClasses = RepositoryScanMarker.class)
@EntityScan(basePackageClasses = EntityScanMarker.class)
public class RelationDbInfraConfig {

}
