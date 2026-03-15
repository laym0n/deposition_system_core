package com.deposition.domain.port.out;

import com.deposition.domain.models.statistics.StatisticsEvent;

public interface StatisticsEventOutPort {

    void save(StatisticsEvent event);
}
