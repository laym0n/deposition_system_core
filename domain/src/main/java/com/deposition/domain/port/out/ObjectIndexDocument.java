package com.deposition.domain.port.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.models.valueobject.Relationship;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ObjectIndexDocument(
        @NotNull
        UUID objectId,
        @NotBlank
        String entityType,
        @NotNull
        ObjectAcl acl,
        @Nullable
        String originalName,
        @Nullable
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
            @Nullable
            String storageVersionId,
            @Nullable
            String blockchainTxId,
            @Nullable
            String anchoredAt) {

    }

}
