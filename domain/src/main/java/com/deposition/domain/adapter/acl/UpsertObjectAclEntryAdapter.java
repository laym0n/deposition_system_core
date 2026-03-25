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

    private static UpsertRightsStatementRequest buildRightsStatementRequest(UUID objectId, String targetUserId,
                                                                            Set<AclPermission> permissions) {

        String rightsStatementId = "acl_" + objectId + "_" + targetUserId + "_" + UUID.randomUUID();

        var rightsGranted = new ArrayList<RightsGranted>();
        if (permissions != null) {
            for (var p : permissions) {
                if (p == null) {
                    continue;
                }
                rightsGranted.add(RightsGranted.builder().act(p.name()).build());
            }
        }

        var payload = new RightsStatementPayload(
                null,
                null,
                null,
                null,
                rightsGranted);

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

        var rsRequest = buildRightsStatementRequest(objectId, request.userId(), request.permissions());
        rightsStatementPremisUpdater.upsertRightsStatement(premis, objectId, rsRequest,
                List.of(toAgentMetadata(rsRequest)));

        // Build ACL from updated PREMIS.
        var snapshot = premisSnapshotConverter.map(premis);
        ObjectAcl computedAcl = AclMapper.buildDefaultAclFromSnapshot(snapshot, objectId);

        return premisPersistenceService.persistPremis(objectId, premis, computedAcl);
    }

    @Override
    public DepositionResult removeUserEntry(UUID objectId, String userId) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }

        accessValidatorService.validateCurrentUserIsSuperAdmin(objectId);

        PremisComplexType premis = loadPremis(objectId);

        var rsRequest = buildRightsStatementRequest(objectId, userId, Set.of());
        rightsStatementPremisUpdater.upsertRightsStatement(premis, objectId, rsRequest,
                List.of(toAgentMetadata(rsRequest)));

        var snapshot = premisSnapshotConverter.map(premis);
        ObjectAcl computedAcl = AclMapper.buildDefaultAclFromSnapshot(snapshot, objectId);

        return premisPersistenceService.persistPremis(objectId, premis, computedAcl);
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
