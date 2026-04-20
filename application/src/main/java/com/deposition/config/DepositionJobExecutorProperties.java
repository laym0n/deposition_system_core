package com.deposition.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "deposition.job.executor")
public record DepositionJobExecutorProperties(
        String threadNamePrefix,
        boolean daemon,
        int corePoolSize,
        int maximumPoolSize,
        Duration keepAlive,
        boolean allowCoreThreadTimeOut) {

    public DepositionJobExecutorProperties {
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("corePoolSize must be >= 0");
        }
        if (maximumPoolSize < 1) {
            throw new IllegalArgumentException("maximumPoolSize must be >= 1");
        }
        if (keepAlive == null) {
            keepAlive = Duration.ofSeconds(60);
        }
    }
}
