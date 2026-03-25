package com.deposition.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "statistics.events.executor")
public record StatisticsEventExecutorProperties(
        String threadNamePrefix,
        boolean daemon,
        int corePoolSize,
        int maximumPoolSize,
        Duration keepAlive,
        boolean allowCoreThreadTimeOut) {

}
