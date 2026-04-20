package com.deposition.infra.s3.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Configuration {

    private static software.amazon.awssdk.services.s3.S3Configuration buildS3ServiceConfiguration() {
        return software.amazon.awssdk.services.s3.S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }

    private static StaticCredentialsProvider buildCredentialsProvider(S3Properties properties) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey()));
    }

    @Bean
    public S3Client s3Client(S3Properties properties) {
        var builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(buildCredentialsProvider(properties));

        if (StringUtils.hasText(properties.getEndpoint())) {
            builder = builder
                    .endpointOverride(URI.create(properties.getEndpoint()))
                    .serviceConfiguration(buildS3ServiceConfiguration());
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties properties) {
        var builder = S3Presigner.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(buildCredentialsProvider(properties));

        if (StringUtils.hasText(properties.getPresignEndpoint())) {
            builder = builder
                    .endpointOverride(URI.create(properties.getPresignEndpoint()))
                    .serviceConfiguration(buildS3ServiceConfiguration());
        }

        return builder.build();
    }
}
