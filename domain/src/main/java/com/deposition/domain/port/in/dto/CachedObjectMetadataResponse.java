package com.deposition.domain.port.in.dto;

import java.util.Map;
import java.util.UUID;

import com.deposition.domain.models.acl.ObjectAcl;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record CachedObjectMetadataResponse(
        @NotNull
        UUID objectId,
        @NotNull
        PremisMetadata premisMetadata,
        @Nullable
        Map<String, Object> descriptiveMetadata,
        @Nullable
        ObjectAcl acl) {

    /**
     * PREMIS-like metadata fields cached in the `objects` OpenSearch index.
     */
    public record PremisMetadata(
            @NotNull
            String entityType,
            @Nullable
            String originalName,
            @Nullable
            java.util.List<com.deposition.domain.port.out.ObjectIndexDocument.Anchor> anchors,
            @Nullable
            java.util.List<com.deposition.domain.models.valueobject.ObjectIdentifier> identifiers,
            @Nullable
            java.util.List<com.deposition.domain.models.valueobject.Relationship> relationships) {

    }
}
