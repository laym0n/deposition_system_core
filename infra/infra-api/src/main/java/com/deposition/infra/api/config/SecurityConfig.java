package com.deposition.infra.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http,
                        Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> oAuth2ResourceServerCustomizer)
                        throws Exception {
                http
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oAuth2ResourceServerCustomizer)
                                .csrf(AbstractHttpConfigurer::disable);

                return http.build();
        }
}
