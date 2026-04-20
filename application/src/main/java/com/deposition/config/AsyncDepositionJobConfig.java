package com.deposition.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
@EnableAsync
@EnableConfigurationProperties(DepositionJobExecutorProperties.class)
public class AsyncDepositionJobConfig {

    @Bean(name = "depositionJobExecutor")
    public Executor depositionJobExecutor(DepositionJobExecutorProperties props) {
        var seq = new AtomicLong(0);
        var prefix = props.threadNamePrefix() == null ? "deposition-job-" : props.threadNamePrefix();
        var daemon = props.daemon();

        var threadFactory = (java.util.concurrent.ThreadFactory) runnable -> {
            var t = new Thread(runnable);
            t.setName(prefix + seq.incrementAndGet());
            t.setDaemon(daemon);
            return t;
        };

        // Similar to AsyncStatisticsConfig: cached pool with bounds.
        var executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(threadFactory);
        executor.setMaximumPoolSize(props.maximumPoolSize());
        executor.setCorePoolSize(props.corePoolSize());
        executor.setKeepAliveTime(props.keepAlive().toMillis(), TimeUnit.MILLISECONDS);
        executor.allowCoreThreadTimeOut(props.allowCoreThreadTimeOut());
        return executor;
    }
}
