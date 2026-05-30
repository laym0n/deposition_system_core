package com.deposition.domain.service.acl;

import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.PremisSnapshot;
import com.deposition.domain.models.RightsStatementMetadata;
import com.deposition.domain.models.acl.*;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.enums.EventType;
import com.deposition.domain.models.enums.ObjectIdentifierType;
import com.deposition.domain.models.valueobject.ApplicableDates;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

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
                .filter(agentIdentifier -> agentIdentifier.getType() == AgentIdentifierType.SYSTEM)
                .map(agentIdentifier -> agentIdentifier.getValue())
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unable to build default ACL: CREATION event agentLinks not found for objectId=" + objectId));

        Map<String, AclEntry> byUserId = new HashMap<>();
        byUserId.put(creatorUserId, AclEntry.builder()
                .principal(AclPrincipal.builder()
                        .type(AclPrincipalType.USER)
                        .id(creatorUserId)
                        .build())
                .role(AclRole.SUPER_ADMIN)
                .permissions(EnumSet.of(AclPermission.READ, AclPermission.WRITE))
                .build());

        if (snapshot.getRightsStatements() != null) {
            for (RightsStatementMetadata rs : snapshot.getRightsStatements()) {
                for (var linkingAgentIdentifier : rs.getLinkingAgentIdentifiers()) {
                    if (linkingAgentIdentifier.getAgentIdentifier().getType() != AgentIdentifierType.SYSTEM) {
                        continue;
                    }
                    var userId = linkingAgentIdentifier.getAgentIdentifier().getValue();
                    EnumSet<AclPermission> permissions = EnumSet.noneOf(AclPermission.class);
                    if (rs.getRightsGranted() != null) {
                        for (var g : rs.getRightsGranted()) {
                            if (g == null || g.getAct() == null || g.getAct().isBlank()) {
                                continue;
                            }

                            if (!isGrantActive(g.getTermOfGrant(), ZonedDateTime.now(ZoneOffset.UTC))) {
                                continue;
                            }
                            try {
                                permissions.add(AclPermission.valueOf(g.getAct()));
                            } catch (RuntimeException ex) {
                            }
                        }
                    }
                    boolean hasPerms = !permissions.isEmpty();

                    AclEntry existing = byUserId.get(userId);
                    AclRole role = existing != null && existing.getRole() != null ? existing.getRole() : AclRole.USER;
                    AclPrincipal principal = existing != null && existing.getPrincipal() != null
                            ? existing.getPrincipal()
                            : AclPrincipal.builder().type(AclPrincipalType.USER).id(userId).build();

                    if (!hasPerms) {
                        if (role == AclRole.SUPER_ADMIN) {
                            byUserId.put(userId, AclEntry.builder()
                                    .principal(principal)
                                    .role(role)
                                    .permissions(EnumSet.noneOf(AclPermission.class))
                                    .build());
                        } else {
                            byUserId.remove(userId);
                        }
                        continue;
                    }

                    byUserId.put(userId, AclEntry.builder()
                            .principal(principal)
                            .role(role)
                            .permissions(EnumSet.copyOf(permissions))
                            .build());
                }
            }
        }

        return ObjectAcl.builder()
                .objectId(objectId)
                .entries(List.copyOf(byUserId.values()))
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
                .anyMatch(identifier -> identifier.getType() == ObjectIdentifierType.SYSTEM
                        && Objects.equals(identifier.getValue(), objectId.toString()));
    }

    private static boolean isGrantActive(ApplicableDates termOfGrant,
                                         ZonedDateTime now) {
        if (termOfGrant == null) {
            return true;
        }
        if (termOfGrant.getStartDate() != null && now != null && termOfGrant.getStartDate().isAfter(now)) {
            return false;
        }
        if (termOfGrant.getEndDate() != null && now != null && !termOfGrant.getEndDate().isAfter(now)) {
            return false;
        }
        return true;
    }
}
