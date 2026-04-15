package com.deposition.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableConfigurationProperties(DepositionIndexingExecutorProperties.class)
public class AsyncDepositionIndexingConfig {

    @Bean(name = "depositionIndexingExecutor")
    public Executor depositionIndexingExecutor(DepositionIndexingExecutorProperties props) {
        var executor = new ThreadPoolTaskExecutor();

        executor.setThreadNamePrefix(props.threadNamePrefix() == null ? "deposition-indexing-" : props.threadNamePrefix());
        executor.setDaemon(props.daemon());
        executor.setCorePoolSize(props.corePoolSize());
        executor.setMaxPoolSize(props.maximumPoolSize());
        executor.setQueueCapacity(props.queueCapacity());
        executor.setKeepAliveSeconds((int) props.keepAlive().toSeconds());
        executor.setAllowCoreThreadTimeOut(props.allowCoreThreadTimeOut());

        // Backpressure: when saturated, run in caller thread to slow down /depone instead of dropping tasks.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
