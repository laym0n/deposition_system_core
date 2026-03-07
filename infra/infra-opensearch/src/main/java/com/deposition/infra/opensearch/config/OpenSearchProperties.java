package com.deposition.infra.opensearch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties(prefix = "integration.opensearch")
@Data
@NoArgsConstructor
public class OpenSearchProperties {

    private boolean enabled;

    private String endpoint;

    private String descriptiveMetadataIndex;

    private String objectIndex;
}
