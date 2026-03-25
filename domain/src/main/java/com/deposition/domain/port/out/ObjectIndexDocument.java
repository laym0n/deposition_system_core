package com.deposition.domain.port.out;

import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.models.valueobject.Relationship;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ObjectIndexDocument(
        @NotNull
        UUID objectId,
        @NotNull
        ObjectAcl acl,
        @Nullable
        String originalName,
        @NotNull
        List<Anchor> anchors,
        @Nullable
        List<ObjectIdentifier> identifiers,
        @Nullable
        List<Relationship> relationships,
        @Nullable
        Map<String, Object> descriptive) {

    /**
     * Anchoring info for a particular stored PREMIS version.
     */
    public record Anchor(
            @NotNull
            String storageVersionId,
            @NotNull
            String blockchainTxId,
            @NotNull
            ZonedDateTime anchoredAt) {

    }

}
