package com.deposition.infra.sso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.keycloak")
public record KeycloakAdminProperties(
        String baseUrl,
        String realm,
        String adminClientId,
        String adminClientSecret) {
}
