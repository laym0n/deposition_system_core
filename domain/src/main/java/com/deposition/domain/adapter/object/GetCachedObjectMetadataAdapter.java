package com.deposition.domain.adapter.object;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.acl.AclEntry;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.port.in.object.CachedObjectMetadataResponse;
import com.deposition.domain.port.in.object.CachedObjectMetadataResponse.PremisMetadata;
import com.deposition.domain.port.in.object.GetCachedObjectMetadataInPort;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.domain.port.out.UserOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@Validated
@RequiredArgsConstructor
public class GetCachedObjectMetadataAdapter implements GetCachedObjectMetadataInPort {

    private final ObjectIndexLookupOutPort objectIndexLookupOutPort;
    private final UserOutPort userOutPort;

    private static List<AclEntry> filterAclEntriesForUser(
            List<AclEntry> entries,
            String userId) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        return entries.stream()
                .filter(Objects::nonNull)
                .filter(e -> e.isForUser(userId))
                .toList();
    }

    @Override
    public CachedObjectMetadataResponse getCachedMetadata(UUID objectId) {
        var doc = objectIndexLookupOutPort.findByObjectId(objectId)
                .orElseThrow(() -> new ResourceNotFoundException("Object", objectId.toString()));

        var premisMetadata = new PremisMetadata(
                doc.premis().originalName(),
                doc.anchors(),
                doc.premis().identifiers(),
                doc.premis().relationships());

        var optionalCurrentUserId = userOutPort.getOptinalCurrentUserId();
        ObjectAcl userAcl = null;
        if (optionalCurrentUserId.isPresent()) {
            userAcl = ObjectAcl.builder()
                    .objectId(doc.objectId())
                    .entries(filterAclEntriesForUser(doc.acl().getEntries(), optionalCurrentUserId.get()))
                    .build();
        }

        return new CachedObjectMetadataResponse(
                doc.objectId(),
                premisMetadata,
                doc.descriptive(),
                userAcl);
    }
}
