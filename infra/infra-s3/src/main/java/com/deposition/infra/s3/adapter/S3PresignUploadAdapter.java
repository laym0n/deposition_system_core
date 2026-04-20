package com.deposition.infra.s3.adapter;

import com.deposition.domain.port.out.PresignUploadOutPort;
import com.deposition.infra.s3.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class S3PresignUploadAdapter implements PresignUploadOutPort {

    /**
     * When the client provides this header on PUT, S3 validates it and persists checksum.
     * Then it becomes available via HEAD (HeadObjectResponse.checksumSHA256()).
     *
     * IMPORTANT: the value must be Base64 of raw SHA-256 bytes (not hex).
     */
    private static final String CHECKSUM_SHA256_HEADER = "x-amz-checksum-sha256";
    private static final String CHECKSUM_SHA256_PLACEHOLDER = "<BASE64_SHA256_OF_PAYLOAD>";

    private final S3Presigner presigner;
    private final S3Properties properties;

    @Override
    public PresignedUpload presignPutObject(PresignPutObjectCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }
        var key = command.objectKey();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("objectKey must not be blank");
        }

        var bucket = properties.getBucketName();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("S3 bucket-name is not configured");
        }

        var putObjectRequestBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key);

        Map<String, String> requiredHeaders = new HashMap<>();
        if (command.contentType() != null && !command.contentType().isBlank()) {
            putObjectRequestBuilder = putObjectRequestBuilder.contentType(command.contentType());
            requiredHeaders.put("Content-Type", command.contentType());
        }

        // We do not sign this header in the presigned URL (so the value can be calculated by the client).
        // But we still return it as required, because Processing expects checksum in S3 HEAD.
        requiredHeaders.put(CHECKSUM_SHA256_HEADER, CHECKSUM_SHA256_PLACEHOLDER);

        var putObjectRequest = putObjectRequestBuilder.build();
        var presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(command.expiresIn())
                .putObjectRequest(putObjectRequest)
                .build();

        var presigned = presigner.presignPutObject(presignRequest);
        URI uploadUrl;
        try {
            uploadUrl = presigned.url().toURI();
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalStateException("Failed to build presigned upload URL", ex);
        }

        // Stable identifier; later we can load it using FileStorageOutPort.loadByContentLocation
        // (S3FileStorageAdapter can extract objectKey from URI).
        URI contentLocation = URI.create("s3://" + bucket + "/" + key);

        var expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(command.expiresIn());
        return new PresignedUpload(uploadUrl, contentLocation, expiresAt, Map.copyOf(requiredHeaders));
    }
}
