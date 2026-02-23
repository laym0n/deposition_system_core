package com.deposition.infra.s3.adapter;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.deposition.domain.models.valueobject.Storage;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.infra.s3.config.S3Properties;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStorageOutPort {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public Storage persist(Resource resource, String entityId) {
        try {
            var objectKey = buildObjectKey(resource, entityId);
            var bucketName = s3Properties.getBucketName();
            var putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(resolveContentType(resource))
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(resource.getInputStream(), resource.contentLength()));

            return Storage.builder()
                    .contentLocation(buildObjectUri(bucketName, objectKey))
                    .build();
        } catch (IOException | SdkException exception) {
            throw new IllegalStateException("Failed to persist resource in S3", exception);
        }
    }

    @SneakyThrows
    private URI buildObjectUri(String bucketName, String objectKey) {
        var endpoint = s3Client.utilities().getUrl(builder -> builder
                .bucket(bucketName)
                .key(objectKey));
        return endpoint.toURI();
    }

    private String buildObjectKey(Resource resource, String entityId) {
        return "object/" + entityId + "/" + resource.getFilename();
    }

    private String resolveContentType(Resource resource) {
        var filename = resource.getFilename();
        var contentType = URLConnection.guessContentTypeFromName(filename);
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }

        return contentType;
    }
}
