package com.deposition.infra.s3.adapter;

import java.io.IOException;
import java.net.URLConnection;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.deposition.domain.models.valueobject.ContentLocation;
import com.deposition.domain.models.valueobject.Storage;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.infra.s3.config.S3Properties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStorageOutPort {

    private static final String CONTENT_LOCATION_TYPE = "s3";

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Override
    public Storage persist(Resource resource) {
        try {
            var objectKey = buildObjectKey(resource);
            var bucketName = s3Properties.getBucketName();
            var putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(resolveContentType(resource))
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(resource.getInputStream(), resource.contentLength()));

            return Storage.builder()
                    .contentLocation(ContentLocation.builder()
                            .contentLocationType(CONTENT_LOCATION_TYPE)
                            .contentLocationValue("s3://" + bucketName + "/" + objectKey)
                            .build())
                    .build();
        } catch (IOException | SdkException exception) {
            throw new IllegalStateException("Failed to persist resource in S3", exception);
        }
    }

    private String buildObjectKey(Resource resource) {
        var filename = resource.getFilename();
        if (filename == null || filename.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return UUID.randomUUID() + "-" + filename;
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
