package com.deposition.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
@EnableAsync
@EnableConfigurationProperties(StatisticsEventExecutorProperties.class)
public class AsyncStatisticsConfig {

    private static ThreadFactory buildThreadFactory(StatisticsEventExecutorProperties properties) {
        var seq = new AtomicLong(0);
        var prefix = properties == null || properties.threadNamePrefix() == null
                ? "statistics-event-"
                : properties.threadNamePrefix();
        var daemon = properties != null && properties.daemon();

        return (runnable) -> {
            var thread = new Thread(runnable);
            thread.setName(prefix + seq.incrementAndGet());
            thread.setDaemon(daemon);
            return thread;
        };
    }

    @Bean(name = "statisticsEventExecutor")
    public Executor statisticsEventExecutor(StatisticsEventExecutorProperties properties) {
        ThreadFactory threadFactory = buildThreadFactory(properties);

        var executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(threadFactory);
        executor.setMaximumPoolSize(properties.maximumPoolSize());
        executor.setCorePoolSize(properties.corePoolSize());
        executor.setKeepAliveTime(properties.keepAlive().toMillis(), TimeUnit.MILLISECONDS);
        executor.allowCoreThreadTimeOut(properties.allowCoreThreadTimeOut());
        return executor;
    }
}
