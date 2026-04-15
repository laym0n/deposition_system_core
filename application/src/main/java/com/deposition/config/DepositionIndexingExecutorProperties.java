package com.deposition.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "deposition.indexing.executor")
public record DepositionIndexingExecutorProperties(
        String threadNamePrefix,
        boolean daemon,
        int corePoolSize,
        int maximumPoolSize,
        int queueCapacity,
        Duration keepAlive,
        boolean allowCoreThreadTimeOut) {
}
