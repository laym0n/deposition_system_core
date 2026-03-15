package com.deposition.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableConfigurationProperties(StatisticsEventExecutorProperties.class)
public class AsyncStatisticsConfig {

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
}
