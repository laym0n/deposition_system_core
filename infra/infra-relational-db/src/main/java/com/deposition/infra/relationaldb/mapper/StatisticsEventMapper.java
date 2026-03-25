package com.deposition.infra.relationaldb.mapper;

import com.deposition.domain.models.statistics.StatisticsEvent;
import com.deposition.infra.relationaldb.entity.StatisticsEventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StatisticsEventMapper {

    @Mapping(target = "eventType", expression = "java(domain.eventType() == null ? null : domain.eventType().name())")
    StatisticsEventEntity toEntity(StatisticsEvent domain);
}
