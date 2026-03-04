package com.deposition.application.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class LocalDevTestContainersConfiguration {
    private static final int LOCAL_DEV_PG_PORT = 55432;

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresDBContainer() {
        var container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("deposition")
                .withUsername("deposition")
                .withPassword("deposition");

        // Make port deterministic so pgAdmin (running in docker-compose) can connect.
        // Equivalent of "-p 55432:5432".
        container.setPortBindings(java.util.List.of(LOCAL_DEV_PG_PORT + ":5432"));

        return container;
    }
}
