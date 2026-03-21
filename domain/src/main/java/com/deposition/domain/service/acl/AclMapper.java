package com.deposition.domain.service.acl;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.PremisSnapshot;
import com.deposition.domain.models.acl.AclEntry;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.AclPrincipal;
import com.deposition.domain.models.acl.AclPrincipalType;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.models.enums.EventType;
import com.deposition.domain.models.enums.ObjectIdentifierType;

@Component
public final class AclMapper {

    public static ObjectAcl buildDefaultAclFromSnapshot(PremisSnapshot snapshot, UUID objectId) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null");
        }
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }

        var creatorUserId = snapshot.getEvents().stream()
                .filter(Objects::nonNull)
                .filter(event -> event.getType() == EventType.CREATION)
                .filter(event -> isEventForObject(event, objectId))
                .flatMap(event -> event.getAgentLinks().stream())
                .filter(Objects::nonNull)
                .map(link -> link.getAgentIdentifier())
                .filter(Objects::nonNull)
                .filter(agentIdentifier -> agentIdentifier.getType() != null
                && agentIdentifier.getType().name().equalsIgnoreCase("LOCAL"))
                .map(agentIdentifier -> agentIdentifier.getValue())
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                "Unable to build default ACL: CREATION event agentLinks not found for objectId=" + objectId));

        return ObjectAcl.builder()
                .objectId(objectId)
                .entries(java.util.List.of(
                        AclEntry.builder()
                                .principal(AclPrincipal.builder()
                                        .type(AclPrincipalType.USER)
                                        .id(creatorUserId)
                                        .build())
                                .permissions(EnumSet.of(AclPermission.READ, AclPermission.WRITE))
                                .build()))
                .build();
    }

    private static boolean isEventForObject(EventMetadata event, UUID objectId) {
        if (event == null || objectId == null || event.getObjectLinks() == null) {
            return false;
        }

        return event.getObjectLinks().stream()
                .filter(Objects::nonNull)
                .map(link -> link.getObjectIdentifier())
                .filter(Objects::nonNull)
                .anyMatch(identifier -> identifier.getType() == ObjectIdentifierType.LOCAL
                && Objects.equals(identifier.getValue(), objectId.toString()));
    }
}
