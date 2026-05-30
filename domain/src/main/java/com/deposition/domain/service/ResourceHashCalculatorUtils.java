package com.deposition.domain.service;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public final class ResourceHashCalculatorUtils {

    public static final String DEFAULT_HASH_ALGORITHM = "SHA-256";

    private ResourceHashCalculatorUtils() {
    }

    public static String sha256(Resource resource) {
        return calculateHash(resource, DEFAULT_HASH_ALGORITHM);
    }

    public static String calculateHash(Resource resource, String algorithm) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource must not be null");
        }

        try (InputStream inputStream = resource.getInputStream()) {
            var digest = MessageDigest.getInstance(algorithm);

            byte[] buffer = new byte[8 * 1024 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) >= 0) {
                if (read == 0) {
                    continue;
                }
                digest.update(buffer, 0, read);
            }

            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to calculate hash for resource", exception);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Failed to calculate hash (unknown algorithm): " + algorithm, exception);
        }
    }

    public static String calculateMerkleRootHash(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            return null;
        }

        try {
            var leafHashes = resources.stream()
                    .map(resource -> calculateHash(resource, DEFAULT_HASH_ALGORITHM))
                    .map(hash -> HexFormat.of().parseHex(hash))
                    .toList();

            return calculateMerkleRootHashFromHashes(leafHashes);
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to calculate Merkle root hash for resources", exception);
        }
    }

    private static String calculateMerkleRootHashFromHashes(List<byte[]> leafHashes) {
        if (leafHashes == null || leafHashes.isEmpty()) {
            return null;
        }

        var currentLevel = new ArrayList<>(leafHashes);

        while (currentLevel.size() > 1) {
            var nextLevel = new ArrayList<byte[]>();
            for (var index = 0; index < currentLevel.size(); index += 2) {
                var left = currentLevel.get(index);
                var right = index + 1 < currentLevel.size() ? currentLevel.get(index + 1) : left;
                nextLevel.add(calculateHashInternal(concatenate(left, right), DEFAULT_HASH_ALGORITHM));
            }
            currentLevel = nextLevel;
        }

        return HexFormat.of().formatHex(currentLevel.get(0));
    }

    private static byte[] calculateHashInternal(byte[] value, String algorithm) {
        try {
            var digest = MessageDigest.getInstance(algorithm);
            return digest.digest(value);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Failed to calculate Merkle hash", exception);
        }
    }

    private static byte[] concatenate(byte[] left, byte[] right) {
        var result = new byte[left.length + right.length];
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }
}
