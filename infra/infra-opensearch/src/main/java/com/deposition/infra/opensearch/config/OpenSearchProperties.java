package com.deposition.infra.opensearch.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.opensearch")
@Data
@NoArgsConstructor
public class OpenSearchProperties {

    private String endpoint;

    private String descriptiveMetadataIndex;

    private String objectIndex;
}
