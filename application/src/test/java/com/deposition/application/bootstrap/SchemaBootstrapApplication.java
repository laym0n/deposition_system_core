package com.deposition.application.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.deposition.application.Application;
import com.deposition.application.config.LocalDevTestContainersConfiguration;

@SpringBootApplication
public class SchemaBootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.from(Application::main)
                .with(LocalDevTestContainersConfiguration.class)
                .run(args);
    }
}
