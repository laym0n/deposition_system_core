package com.deposition.infra.s3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@ConfigurationProperties(prefix = "integration.s3")
@Data
@NoArgsConstructor
public class S3Properties {

    private String endpoint;
    private String region;
    private String accessKey;
    private String secretKey;
    private String bucketName;
}
