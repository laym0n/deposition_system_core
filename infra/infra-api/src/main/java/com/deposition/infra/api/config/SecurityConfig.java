package com.deposition.infra.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(EndpointAccessProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           EndpointAccessProperties endpointAccessProperties,
                                           Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> oAuth2ResourceServerCustomizer) {
        http
                .authorizeHttpRequests(authz -> {
                    authz
                            .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**",
                                    "/swagger-ui.html")
                            .permitAll()
                            .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                            .requestMatchers(HttpMethod.GET, "/descriptive-metadata/schemas/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/objects/*/cached-metadata").permitAll();

                    endpointAccessProperties.getRules().forEach(rule -> {
                        var access = rule.accessType();
                        if (rule.getPath() == null || rule.getPath().isBlank()) {
                            throw new IllegalArgumentException("Endpoint access rule path must not be blank");
                        }

                        var matcher = rule.getMethod() == null
                                ? authz.requestMatchers(rule.getPath())
                                : authz.requestMatchers(rule.getMethod(), rule.getPath());

                        switch (access) {
                            case AUTHENTICATED -> matcher.authenticated();
                            case ANONYMOUS -> matcher.permitAll();
                            case ROLE -> matcher.hasRole(rule.roleName());
                        }
                    });

                    authz.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oAuth2ResourceServerCustomizer)
                .cors(AbstractHttpConfigurer::disable)
                .csrf(Customizer.withDefaults());
        return http.build();
    }
}
