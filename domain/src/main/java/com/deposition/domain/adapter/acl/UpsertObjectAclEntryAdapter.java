package com.deposition.domain.adapter.acl;

import com.deposition.domain.adapter.rights.RightsStatementPremisUpdater;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.models.enums.AgentType;
import com.deposition.domain.models.enums.RightsBasis;
import com.deposition.domain.models.valueobject.ApplicableDates;
import com.deposition.domain.models.valueobject.RightsGranted;
import com.deposition.domain.port.in.acl.UpsertObjectAclEntryInPort;
import com.deposition.domain.port.in.acl.UpsertObjectAclEntryRequest;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest.AgentDto;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest.AgentGrant;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest.RightsStatementPayload;
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

    private UpsertRightsStatementRequest buildRightsStatementRequest(PremisComplexType premis,
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

        var payload = new RightsStatementPayload(
                null,
                null,
                null,
                null,
                merged);

        var agent = new AgentDto(
                targetUserId,
                targetUserId,
                AgentType.PERSON,
                List.of());

        var agentGrant = new AgentGrant(agent, List.of("GRANTEE"));

        return new UpsertRightsStatementRequest(
                rightsStatementId,
                RightsBasis.OTHER,
                payload,
                List.of(agentGrant));
    }

    private DepositionResult upsertRightsAndPersistAcl(UUID objectId, PremisComplexType premis,
                                                       UpsertRightsStatementRequest rsRequest) {
        rightsStatementPremisUpdater.upsertRightsStatement(premis, objectId, rsRequest,
                toAgentMetadataList(rsRequest));

        var snapshot = premisSnapshotConverter.map(premis);
        ObjectAcl computedAcl = AclMapper.buildDefaultAclFromSnapshot(snapshot, objectId);

        return premisPersistenceService.persistPremis(objectId, premis, computedAcl);
    }

    private static AgentMetadata toAgentMetadata(UpsertRightsStatementRequest request) {
        if (request == null || request.agents() == null || request.agents().isEmpty()) {
            return null;
        }
        AgentGrant grant = request.agents().getFirst();
        if (grant == null || grant.agent() == null) {
            return null;
        }
        var a = grant.agent();
        return AgentMetadata.builder()
                .id(a.id())
                .name(a.name())
                .type(a.type())
                .identifiers(a.identifiers() == null ? List.of() : List.copyOf(a.identifiers()))
                .build();
    }

    private static List<AgentMetadata> toAgentMetadataList(UpsertRightsStatementRequest request) {
        AgentMetadata agent = toAgentMetadata(request);
        if (agent == null) {
            return List.of();
        }
        return List.of(agent);
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

        var rsRequest = buildRightsStatementRequest(premis, objectId, request.userId(), request.permissions());
        return upsertRightsAndPersistAcl(objectId, premis, rsRequest);
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
