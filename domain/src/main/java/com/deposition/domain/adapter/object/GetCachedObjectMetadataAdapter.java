package com.deposition.domain.adapter.object;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.port.in.object.GetCachedObjectMetadataInPort;
import com.deposition.domain.port.in.object.CachedObjectMetadataResponse;
import com.deposition.domain.port.in.object.CachedObjectMetadataResponse.PremisMetadata;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Component
@Validated
@RequiredArgsConstructor
public class GetCachedObjectMetadataAdapter implements GetCachedObjectMetadataInPort {

    private final ObjectIndexLookupOutPort objectIndexLookupOutPort;

    @Override
    public CachedObjectMetadataResponse getCachedMetadata(UUID objectId, @Nullable String currentUserId) {
        var doc = objectIndexLookupOutPort.findByObjectId(objectId)
                .orElseThrow(() -> new ResourceNotFoundException("Object", objectId.toString()));

        var premisMetadata = new PremisMetadata(
                doc.entityType(),
                doc.originalName(),
                doc.anchors(),
                doc.identifiers(),
                doc.relationships());

        ObjectAcl userAcl = null;
        if (currentUserId != null && !currentUserId.isBlank() && doc.acl() != null) {
            userAcl = ObjectAcl.builder()
                    .objectId(doc.objectId())
                    .entries(filterAclEntriesForUser(doc.acl().getEntries(), currentUserId))
                    .build();
        }

        return new CachedObjectMetadataResponse(
                doc.objectId(),
                premisMetadata,
                doc.descriptive(),
                userAcl);
    }

    private static List<com.deposition.domain.models.acl.AclEntry> filterAclEntriesForUser(
            List<com.deposition.domain.models.acl.AclEntry> entries,
            String userId) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        return entries.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.isForUser(userId))
                .toList();
    }
}
