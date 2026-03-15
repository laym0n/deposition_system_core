package com.deposition.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "statistics.events.executor")
public record StatisticsEventExecutorProperties(
        String threadNamePrefix,
        boolean daemon,
        int corePoolSize,
        int maximumPoolSize,
        Duration keepAlive,
        boolean allowCoreThreadTimeOut) {

}
