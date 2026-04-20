package com.deposition.infra.s3.adapter;

import com.deposition.domain.models.valueobject.Storage;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.infra.s3.config.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.DigestInputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStorageOutPort {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    private static String buildPremisObjectKey(UUID objectId) {
        return "object/" + objectId + "/deposition-metadata.premis.xml";
    }

    static String extractObjectKey(URI contentLocation, String bucketName) {
        String path = contentLocation.getPath();
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Invalid contentLocation (path is empty): " + contentLocation);
        }
        String normalized = path.startsWith("/") ? path.substring(1) : path;

        if (bucketName != null && !bucketName.isBlank()) {
            String bucketPrefix = bucketName + "/";
            if (normalized.startsWith(bucketPrefix)) {
                normalized = normalized.substring(bucketPrefix.length());
            }
        }

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }

    private static String extractFilename(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        int idx = objectKey.lastIndexOf('/');
        if (idx < 0 || idx == objectKey.length() - 1) {
            return objectKey;
        }
        return objectKey.substring(idx + 1);
    }

    private Resource loadObjectAsStreamingResource(String bucketName, String objectKey, String versionId, String filename) {
        // We return a Resource that opens a NEW S3 stream on each getInputStream() call.
        return new AbstractResource() {
            @Override
            public String getDescription() {
                return "S3 object: bucket=" + bucketName + ", key=" + objectKey + ", versionId=" + versionId;
            }

            @Override
            public String getFilename() {
                return filename;
            }

            @Override
            public InputStream getInputStream() {
                try {
                    var req = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .versionId(versionId)
                            .build();
                    ResponseInputStream<GetObjectResponse> in = s3Client.getObject(req);
                    // Note: caller is responsible for closing stream.
                    return in;
                } catch (NoSuchKeyException ex) {
                    throw new IllegalArgumentException(
                            "Object not found in S3: bucket=" + bucketName + ", key=" + objectKey,
                            ex);
                } catch (SdkException ex) {
                    throw new IllegalStateException(
                            "Failed to open S3 object stream: bucket=" + bucketName + ", key=" + objectKey,
                            ex);
                }
            }

            @Override
            public long contentLength() {
                try {
                    var headReq = HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .versionId(versionId)
                            .build();
                    var head = s3Client.headObject(headReq);
                    return head.contentLength();
                } catch (SdkException ex) {
                    throw new IllegalStateException(
                            "Failed to read S3 object metadata (contentLength): bucket=" + bucketName + ", key=" + objectKey,
                            ex);
                }
            }
        };
    }

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

            var putResponse = s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(resource.getInputStream(), resource.contentLength()));

            return Storage.builder()
                    .contentLocation(buildObjectUri(bucketName, objectKey))
                    .versionId(putResponse.versionId())
                    .build();
        } catch (IOException | SdkException exception) {
            throw new IllegalStateException("Failed to persist resource in S3", exception);
        }
    }

    @Override
    public PersistedResource persistWithDigest(Resource resource, String entityId, String hashAlgorithm) {
        if (hashAlgorithm == null || hashAlgorithm.isBlank()) {
            throw new IllegalArgumentException("hashAlgorithm must not be blank");
        }

        try {
            var objectKey = buildObjectKey(resource, entityId);
            var bucketName = s3Properties.getBucketName();
            var putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(resolveContentType(resource))
                    .build();

            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance(hashAlgorithm);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("Unsupported digest algorithm: " + hashAlgorithm, e);
            }

            // For file-backed resources this is a metadata lookup (stat) and does not require reading the stream.
            long size = resource.contentLength();

            try (var raw = resource.getInputStream(); var in = new DigestInputStream(raw, digest)) {
                var putResponse = s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(in, size));

                var storage = Storage.builder()
                        .contentLocation(buildObjectUri(bucketName, objectKey))
                        .versionId(putResponse.versionId())
                        .build();

                return new PersistedResource(storage, hashAlgorithm, digest.digest(), size);
            }
        } catch (IOException | SdkException exception) {
            throw new IllegalStateException("Failed to persist resource in S3", exception);
        }
    }

    @Override
    public Resource loadPremisMetadataByObjectId(UUID objectId) {
        return loadPremisMetadataByObjectId(objectId, null);
    }

    @Override
    public Resource loadPremisMetadataByObjectId(UUID objectId, String versionId) {
        try {
            var objectKey = buildPremisObjectKey(objectId);
            var bucketName = s3Properties.getBucketName();

            return loadObjectAsStreamingResource(
                    bucketName,
                    objectKey,
                    versionId,
                    "deposition-metadata.premis.xml");
        } catch (NoSuchKeyException exception) {
            throw new IllegalArgumentException("PREMIS metadata not found for objectId=" + objectId, exception);
        } catch (SdkException exception) {
            throw new IllegalStateException("Failed to load PREMIS metadata from S3 for objectId=" + objectId,
                    exception);
        }
    }

    @Override
    public Resource loadByContentLocation(URI contentLocation) {
        if (contentLocation == null) {
            throw new IllegalArgumentException("contentLocation must not be null");
        }

        var bucketName = s3Properties.getBucketName();
        String objectKey = extractObjectKey(contentLocation, bucketName);

        try {
            return loadObjectAsStreamingResource(
                    bucketName,
                    objectKey,
                    null,
                    extractFilename(objectKey));
        } catch (NoSuchKeyException exception) {
            throw new IllegalArgumentException(
                    "Object not found in S3: bucket=" + bucketName + ", key=" + objectKey
                            + ", contentLocation=" + contentLocation,
                    exception);
        } catch (SdkException exception) {
            throw new IllegalStateException(
                    "Failed to load object from S3: bucket=" + bucketName + ", key=" + objectKey
                            + ", contentLocation=" + contentLocation,
                    exception);
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
