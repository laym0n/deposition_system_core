package com.deponic.domain.adapter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.deponic.domain.models.ObjectMetadata;
import com.deponic.domain.models.valueobject.FixityBlock;
import com.deponic.domain.port.in.DeponeInPort;
import com.deponic.domain.port.in.DeponeObjectParams;
import com.deponic.domain.port.out.FileStorageOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeponeAdapter implements DeponeInPort {

    private static final String HASH_ALGORITHM = "SHA-256";
    private final FileStorageOutPort fileStorage;

    @Override
    public void depone(DeponeObjectParams params) {
        if (params == null || params.files() == null) {
            return;
        }

        for (var fileParam : params.files()) {
            if (fileParam == null || fileParam.resource() == null) {
                continue;
            }

            var calculatedHash = calculateHash(fileParam.resource());
            var metadata = new ObjectMetadata();
            metadata.getCharacteristics().getFirst().getFixity()
                    .add(new FixityBlock(HASH_ALGORITHM, calculatedHash.hash()));

            var storage = fileStorage.persist(fileParam.resource());
            metadata.setStorages(fileParam.storages());
            metadata.getStorages().add(storage);
        }
    }

    private HashCalculationResult calculateHash(Resource resource) {
        try {
            var messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
            var totalBytes = 0L;

            try (var inputStream = resource.getInputStream()) {
                var buffer = new byte[8192];
                var bytesRead = inputStream.read(buffer);
                while (bytesRead != -1) {
                    messageDigest.update(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    bytesRead = inputStream.read(buffer);
                }
            }

            var hash = HexFormat.of().formatHex(messageDigest.digest());
            return new HashCalculationResult(hash, totalBytes);
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Failed to calculate hash for deposition file", exception);
        }
    }

    private record HashCalculationResult(String hash, long size) {

    }
}
