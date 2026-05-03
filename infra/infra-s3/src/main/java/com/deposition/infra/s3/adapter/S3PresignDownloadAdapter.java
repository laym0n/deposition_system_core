package com.deposition.infra.s3.adapter;

import com.deposition.domain.port.out.PresignDownloadOutPort;
import com.deposition.infra.s3.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class S3PresignDownloadAdapter implements PresignDownloadOutPort {

    private final S3Presigner presigner;
    private final S3Properties properties;

    @Override
    public PresignedDownload presignGetObject(PresignGetObjectCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        if (command.contentLocation() == null) {
            throw new IllegalArgumentException("contentLocation must not be null");
        }

        var bucket = properties.getBucketName();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("S3 bucket-name is not configured");
        }

        String key = S3FileStorageAdapter.extractObjectKey(command.contentLocation(), bucket);

        var getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(command.expiresIn())
                .getObjectRequest(getObjectRequest)
                .build();

        var presigned = presigner.presignGetObject(presignRequest);
        URI downloadUrl;
        try {
            downloadUrl = presigned.url().toURI();
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalStateException("Failed to build presigned download URL", ex);
        }

        var expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(command.expiresIn());
        return new PresignedDownload(downloadUrl, command.contentLocation(), expiresAt);
    }
}
