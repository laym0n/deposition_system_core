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
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.ResponseInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.DigestInputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HexFormat;
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
    public FileAttributes getAttributesByContentLocation(URI contentLocation, String hashAlgorithm) {
        if (contentLocation == null) {
            throw new IllegalArgumentException("contentLocation must not be null");
        }
        if (hashAlgorithm == null || hashAlgorithm.isBlank()) {
            throw new IllegalArgumentException("hashAlgorithm must not be blank");
        }

        var bucketName = s3Properties.getBucketName();
        String objectKey = extractObjectKey(contentLocation, bucketName);

        HeadObjectResponse head;
        try {
            var headReq = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .checksumMode(ChecksumMode.ENABLED)
                    .build();
            head = s3Client.headObject(headReq);
        } catch (NoSuchKeyException ex) {
            throw new IllegalArgumentException(
                    "Object not found in S3: bucket=" + bucketName + ", key=" + objectKey + ", contentLocation=" + contentLocation,
                    ex);
        } catch (SdkException ex) {
            throw new IllegalStateException(
                    "Failed to load S3 object attributes: bucket=" + bucketName + ", key=" + objectKey + ", contentLocation=" + contentLocation,
                    ex);
        }

        long sizeBytes = head.contentLength() == null ? -1 : head.contentLength();

        String digestHex = resolveDigestHexFromHead(head, hashAlgorithm);
        if (digestHex == null) {
            throw new IllegalStateException(
                    "S3 object does not contain checksum for algorithm=" + hashAlgorithm
                            + ". Configure client upload to send checksum (e.g. x-amz-checksum-sha256)"
                            + ", bucket=" + bucketName + ", key=" + objectKey);
        }

        return new FileAttributes(hashAlgorithm, digestHex, sizeBytes);
    }

    private static String resolveDigestHexFromHead(HeadObjectResponse head, String hashAlgorithm) {
        if (head == null) {
            return null;
        }

        String algo = hashAlgorithm.trim().toUpperCase();

        String base64;
        switch (algo) {
            case "SHA-256" -> base64 = head.checksumSHA256();
            case "SHA-1" -> base64 = head.checksumSHA1();
            case "CRC32" -> base64 = head.checksumCRC32();
            case "CRC32C" -> base64 = head.checksumCRC32C();
            default -> base64 = null;
        }

        if (base64 == null || base64.isBlank()) {
            return null;
        }

        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return HexFormat.of().formatHex(raw);
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
