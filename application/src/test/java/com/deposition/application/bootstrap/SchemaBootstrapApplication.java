package com.deposition.application.bootstrap;

import com.deposition.application.Application;
import com.deposition.application.config.LocalDevTestContainersConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SchemaBootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main)
                .with(LocalDevTestContainersConfiguration.class)
                .run(args);
    }
}
