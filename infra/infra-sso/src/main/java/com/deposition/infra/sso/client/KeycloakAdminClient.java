package com.deposition.infra.sso.client;

import com.deposition.infra.sso.config.KeycloakAdminProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private final RestClient restClient;
    private final KeycloakAdminProperties props;

    public List<KeycloakUserRepresentation> searchUsers(String search, int first, int max) {
        var token = obtainAdminToken();

        return restClient.get()
                .uri(props.baseUrl() + "/admin/realms/" + props.realm() + "/users", uriBuilder -> uriBuilder
                        .queryParam("search", search)
                        .queryParam("first", first)
                        .queryParam("max", max)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {
                });
    }

    private String obtainAdminToken() {
        // Keycloak token endpoint supports client_credentials for confidential clients.
        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", props.adminClientId());
        form.add("client_secret", props.adminClientSecret());

        var token = restClient.post()
                .uri(props.baseUrl() + "/realms/" + props.realm() + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(KeycloakTokenResponse.class);

        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
            throw new IllegalStateException("Failed to obtain Keycloak admin access token");
        }

        return token.accessToken();
    }

    public record KeycloakTokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    public record KeycloakUserRepresentation(
            String id,
            String username) {
    }
}
