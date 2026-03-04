package com.deposition.infra.opensearch.config;

import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

@Configuration
@EnableConfigurationProperties(OpenSearchProperties.class)
@ConditionalOnProperty(prefix = "integration.opensearch", name = "enabled", havingValue = "true")
public class OpenSearchConfiguration {

    @Bean(destroyMethod = "close")
    @SneakyThrows
    public RestClient openSearchRestClient(OpenSearchProperties properties) {
        return RestClient.builder(HttpHost.create(properties.getEndpoint()))
                .build();
    }

    @Bean
    public OpenSearchTransport openSearchTransport(RestClient restClient, ObjectMapper objectMapper) {
        var mapper = new JacksonJsonpMapper(objectMapper);
        return new RestClientTransport(restClient, mapper);
    }

    @Bean
    public OpenSearchClient openSearchClient(OpenSearchTransport transport) {
        return new OpenSearchClient(transport);
    }
}
