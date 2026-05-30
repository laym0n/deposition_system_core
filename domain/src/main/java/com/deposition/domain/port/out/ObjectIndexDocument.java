package com.deposition.domain.port.out;

import com.deposition.domain.models.acl.ObjectAcl;
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
        String intellectualEntityTypeName,
        @NotNull
        ObjectAcl acl,
        @NotNull
        List<Anchor> anchors,
        @Nullable
        Visibility visibility,
        @NotNull
        PremisIndexFields premis,
        @Nullable
        Map<String, Object> descriptive) {

    public ObjectIndexDocument {
        if (visibility == null) {
            visibility = Visibility.PRIVATE;
        }

        if (premis == null) {
            premis = new PremisIndexFields(objectId, null, null, null);
        }
    }

    public record Anchor(
            @NotNull
            String storageVersionId,
            @NotNull
            String blockchainTxId,
            @NotNull
            ZonedDateTime anchoredAt) {

    }

    public enum Visibility {
        PUBLIC,
        PRIVATE;
    }

}
