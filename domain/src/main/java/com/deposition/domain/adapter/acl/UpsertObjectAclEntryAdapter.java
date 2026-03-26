package com.deposition.domain.adapter.acl;

import com.deposition.domain.adapter.rights.RightsStatementPremisUpdater;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.RightsStatementMetadata;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.enums.AgentType;
import com.deposition.domain.models.enums.RightsBasis;
import com.deposition.domain.models.valueobject.AgentIdentifier;
import com.deposition.domain.models.valueobject.ApplicableDates;
import com.deposition.domain.models.valueobject.RightsGranted;
import com.deposition.domain.models.valueobject.RightsStatementAgentLink;
import com.deposition.domain.port.in.acl.UpsertObjectAclEntryInPort;
import com.deposition.domain.port.in.acl.UpsertObjectAclEntryRequest;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.service.PremisPersistenceService;
import com.deposition.domain.service.XmlUtils;
import com.deposition.domain.service.acl.AccessValidatorService;
import com.deposition.domain.service.acl.AclMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Updates ACL entry for a specific user and records the change in PREMIS as a
 * RightsStatement.
 */
@Component
@Validated
@RequiredArgsConstructor
public class UpsertObjectAclEntryAdapter implements UpsertObjectAclEntryInPort {

    private final AccessValidatorService accessValidatorService;
    private final FileStorageOutPort fileStorage;
    private final RightsStatementPremisUpdater rightsStatementPremisUpdater;
    private final PremisPersistenceService premisPersistenceService;
    private final PremisSnapshotConverter premisSnapshotConverter;

    private static boolean isGrantActive(ApplicableDates termOfGrant, ZonedDateTime now) {
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

    private static ApplicableDates ensureTermOfGrant(RightsGranted grant, ZonedDateTime now) {
        if (grant == null) {
            return ApplicableDates.builder().startDate(now).endDate(null).build();
        }
        ApplicableDates dates = grant.getTermOfGrant();
        if (dates == null) {
            dates = ApplicableDates.builder().startDate(now).endDate(null).build();
            grant.setTermOfGrant(dates);
            return dates;
        }
        if (dates.getStartDate() == null) {
            dates.setStartDate(now);
        }
        return dates;
    }

    private RightsStatementMetadata buildRightsStatementMetadata(PremisComplexType premis,
                                                                     UUID objectId,
                                                                     String targetUserId,
                                                                     Set<AclPermission> permissions) {
        if (premis == null) {
            throw new IllegalArgumentException("premis must not be null");
        }
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (targetUserId == null || targetUserId.isBlank()) {
            throw new IllegalArgumentException("targetUserId must not be blank");
        }

        // Deterministic id: we update the same rightsStatement over time, preserving rightsGranted history.
        String rightsStatementId = "acl_" + objectId + "_" + targetUserId;

        // Load existing rightsGranted for this ACL statement (if any).
        var snapshot = premisSnapshotConverter.map(premis);
        List<RightsGranted> existingRightsGranted = snapshot.getRightsStatements().stream()
                .filter(rs -> rs != null && rightsStatementId.equals(rs.getId()))
                .findFirst()
                .map(rs -> rs.getRightsGranted())
                .orElse(List.of());

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        var desiredActs = new java.util.HashSet<String>();
        if (permissions != null) {
            for (var p : permissions) {
                if (p != null) {
                    desiredActs.add(p.name());
                }
            }
        }

        var merged = new ArrayList<RightsGranted>();
        var activeActs = new java.util.HashSet<String>();

        for (var g : existingRightsGranted) {
            if (g == null || g.getAct() == null || g.getAct().isBlank()) {
                continue;
            }

            String act = g.getAct();
            ApplicableDates dates = ensureTermOfGrant(g, now);
            boolean isActive = isGrantActive(dates, now);

            if (desiredActs.contains(act)) {
                if (isActive) {
                    dates.setEndDate(null);
                    activeActs.add(act);
                }
                merged.add(g);
            } else {
                if (isActive) {
                    dates.setEndDate(now);
                }
                merged.add(g);
            }
        }

        // Add missing active grants.
        for (String act : desiredActs) {
            if (act == null || act.isBlank()) {
                continue;
            }
            if (activeActs.contains(act)) {
                continue;
            }
            merged.add(RightsGranted.builder()
                    .act(act)
                    .termOfGrant(ApplicableDates.builder().startDate(now).endDate(null).build())
                    .build());
        }

        var model = RightsStatementMetadata.builder().build();
        model.setId(rightsStatementId);
        model.setRightsBasis(RightsBasis.OTHER.name());
        model.setRightsGranted(merged);

        // Link by SYSTEM userId for ACL use-case.
        model.setLinkingAgentIdentifiers(List.of(RightsStatementAgentLink.builder()
                .agentIdentifier(new AgentIdentifier(AgentIdentifierType.SYSTEM, targetUserId))
                .roles(new LinkedHashSet<>(List.of("GRANTEE")))
                .build()));

        return model;
    }

    private DepositionResult upsertRightsAndPersistAcl(UUID objectId,
                                                       PremisComplexType premis,
                                                       RightsStatementMetadata rightsStatement) {
        String targetUserId = targetUserIdFromRightsStatement(rightsStatement);
        if (targetUserId == null || targetUserId.isBlank()) {
            throw new IllegalArgumentException("targetUserId must not be blank (from rightsStatement.linkingAgentIdentifiers)");
        }

        // Ensure agent by current user id (system) for ACL use-case.
        var ensureAgent = AgentMetadata.builder()
                .id(targetUserId)
                .name(targetUserId)
                .type(AgentType.PERSON)
                .build();

        rightsStatementPremisUpdater.upsertRightsStatement(premis, objectId, rightsStatement, List.of(ensureAgent));

        var snapshot = premisSnapshotConverter.map(premis);
        ObjectAcl computedAcl = AclMapper.buildDefaultAclFromSnapshot(snapshot, objectId);

        return premisPersistenceService.persistPremis(objectId, premis, computedAcl);
    }

    private static String targetUserIdFromRightsStatement(RightsStatementMetadata rightsStatement) {
        if (rightsStatement == null || rightsStatement.getLinkingAgentIdentifiers() == null) {
            return null;
        }
        for (var link : rightsStatement.getLinkingAgentIdentifiers()) {
            if (link == null || link.getAgentIdentifier() == null) {
                continue;
            }
            if (link.getAgentIdentifier().getType() == AgentIdentifierType.SYSTEM) {
                var v = link.getAgentIdentifier().getValue();
                if (v != null && !v.isBlank()) {
                    return v;
                }
            }
        }
        return null;
    }

    @Override
    public DepositionResult upsertUserEntry(UUID objectId, UpsertObjectAclEntryRequest request) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.userId() == null || request.userId().isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }

        // Only object SUPER_ADMIN can change object ACL.
        accessValidatorService.validateCurrentUserIsSuperAdmin(objectId);

        PremisComplexType premis = loadPremis(objectId);

        var rightsStatement = buildRightsStatementMetadata(premis, objectId, request.userId(), request.permissions());
        return upsertRightsAndPersistAcl(objectId, premis, rightsStatement);
    }

    private PremisComplexType loadPremis(UUID objectId) {
        Resource premisXml;
        try {
            premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Object", objectId.toString());
        }
        return XmlUtils.parsePremis(premisXml);
    }
}
