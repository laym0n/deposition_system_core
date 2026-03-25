package com.deposition.infra.relationaldb.repository;

import com.deposition.infra.relationaldb.entity.StatisticsEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StatisticsEventJpaRepository extends JpaRepository<StatisticsEventEntity, UUID> {

}
