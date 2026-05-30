package com.deposition.domain.port.out;

import com.deposition.domain.models.valueobject.Storage;
import jakarta.annotation.Nullable;
import org.springframework.core.io.Resource;

import java.net.URI;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

/**
 * Выходной порт: FileStorageOutPort.
 */
public interface FileStorageOutPort {

    Storage persist(Resource resource, String entityId);

    PersistedResource persistWithDigest(Resource resource, String entityId, String hashAlgorithm);

    Resource loadPremisMetadataByObjectId(UUID objectId);

    Resource loadPremisMetadataByObjectId(UUID objectId, @Nullable String versionId);

    Resource loadByContentLocation(URI contentLocation);

    FileAttributes getAttributesByContentLocation(URI contentLocation, String hashAlgorithm);

    record FileAttributes(
            String hashAlgorithm,
            String digestHex,
            long sizeBytes) {
    }

    record PersistedResource(
            Storage storage,
            String hashAlgorithm,
            byte[] digest,
            long sizeBytes) {

        public PersistedResource {
            Objects.requireNonNull(storage, "storage must not be null");
            Objects.requireNonNull(hashAlgorithm, "hashAlgorithm must not be null");
            Objects.requireNonNull(digest, "digest must not be null");
        }

        public String digestHex() {
            return HexFormat.of().formatHex(digest);
        }
    }

}
