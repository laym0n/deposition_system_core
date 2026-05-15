package com.deposition.domain.adapter.object;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.acl.AclEntry;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.models.statistics.StatisticsEventType;
import com.deposition.domain.port.in.object.CachedObjectMetadataResponse;
import com.deposition.domain.port.in.object.CachedObjectMetadataResponse.PremisMetadata;
import com.deposition.domain.port.in.object.GetCachedObjectMetadataInPort;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.domain.port.out.UserOutPort;
import com.deposition.domain.service.StatisticsEventReporter;
import com.deposition.domain.service.IntellectualEntityTypeResolver;
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
    private final StatisticsEventReporter statisticsEventReporter;
    private final IntellectualEntityTypeResolver intellectualEntityTypeResolver;

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

    private static List<AclEntry> safeEntries(ObjectAcl acl) {
        if (acl == null || acl.getEntries() == null) {
            return List.of();
        }
        return acl.getEntries();
    }

    @Override
    public CachedObjectMetadataResponse getCachedMetadata(UUID objectId) {
        var doc = objectIndexLookupOutPort.findByObjectId(objectId)
                .orElseThrow(() -> new ResourceNotFoundException("Object", objectId.toString()));

        userOutPort.getOptinalCurrentUserId()
                .ifPresent(userId -> statisticsEventReporter.report(
                        StatisticsEventType.OBJECT_VIEW,
                        objectId,
                        null,
                        userId));

        var premisMetadata = new PremisMetadata(
                doc.premis().originalName(),
                doc.anchors(),
                doc.premis().identifiers(),
                doc.premis().relationships());

        var entityType = intellectualEntityTypeResolver.resolveByName(doc.intellectualEntityTypeName());

        var optionalCurrentUserId = userOutPort.getOptinalCurrentUserId();
        ObjectAcl userAcl = null;
        if (optionalCurrentUserId.isPresent()) {
            var currentUserId = optionalCurrentUserId.get();

            // If current user has WRITE permission, return full ACL (rights for all users).
            // Otherwise, keep backward compatible behavior: return only current user's entries.
            var hasWrite = doc.acl() != null && doc.acl().hasPermissionForUser(currentUserId, AclPermission.WRITE);
            var entriesToReturn = hasWrite
                    ? safeEntries(doc.acl())
                    : filterAclEntriesForUser(safeEntries(doc.acl()), currentUserId);

            userAcl = ObjectAcl.builder()
                    .objectId(doc.objectId())
                    .entries(entriesToReturn)
                    .build();
        }

        return new CachedObjectMetadataResponse(
                doc.objectId(),
                entityType,
                premisMetadata,
                doc.descriptive(),
                userAcl);
    }
}
