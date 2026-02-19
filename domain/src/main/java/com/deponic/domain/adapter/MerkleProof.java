package com.deponic.domain.adapter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public final class MerkleProof {

    private static final String HASH_ALGORITHM = "SHA-256";

    private MerkleProof() {
    }

    public static String calculateRootHash(List<byte[]> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return null;
        }

        var currentLevel = payloads.stream()
                .map(MerkleProof::sha256)
                .toList();

        while (currentLevel.size() > 1) {
            var nextLevel = new ArrayList<byte[]>();
            for (var index = 0; index < currentLevel.size(); index += 2) {
                var left = currentLevel.get(index);
                var right = index + 1 < currentLevel.size() ? currentLevel.get(index + 1) : left;
                nextLevel.add(sha256(concatenate(left, right)));
            }
            currentLevel = nextLevel;
        }

        return HexFormat.of().formatHex(currentLevel.get(0));
    }

    private static byte[] sha256(byte[] value) {
        try {
            var digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return digest.digest(value);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Failed to calculate MerkleProof hash", exception);
        }
    }

    private static byte[] concatenate(byte[] left, byte[] right) {
        var result = new byte[left.length + right.length];
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }
}
