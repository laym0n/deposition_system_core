package com.deposition.domain.adapter.rights;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AgentMetadata;

import com.deposition.domain.port.in.rights.UpsertRightsStatementInPort;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.service.PremisPersistenceService;
import com.deposition.domain.service.XmlUtils;
import com.deposition.domain.service.acl.AccessValidatorService;

import lombok.RequiredArgsConstructor;

@Component
@Validated
@RequiredArgsConstructor
public class UpsertRightsStatementAdapter implements UpsertRightsStatementInPort {

    private final FileStorageOutPort fileStorage;
    private final RightsStatementPremisUpdater rightsStatementPremisUpdater;
    private final AccessValidatorService accessValidatorService;
    private final PremisPersistenceService premisPersistenceService;

    @Override
    public DepositionResult upsertRightsStatement(UUID objectId, UpsertRightsStatementRequest request) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        accessValidatorService.validateCurrentUserIsSuperAdmin(objectId);

        Resource premisXml;
        try {
            premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Object", objectId.toString());
        }

        var premis = XmlUtils.parsePremis(premisXml);

        var ensureAgents = resolveAgentsToEnsure(request);
        rightsStatementPremisUpdater.upsertRightsStatement(premis, objectId, request, ensureAgents);

        return premisPersistenceService.persistPremis(objectId, premis);
    }

    @Override
    public DepositionResult updateRightsStatement(UUID objectId, String rightsStatementId,
            UpsertRightsStatementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        // Ensure request is consistent with path param.
        var normalizedRequest = new UpsertRightsStatementRequest(
                rightsStatementId,
                request.rightsBasis(),
                request.payload(),
                request.agents());

        return upsertRightsStatement(objectId, normalizedRequest);
    }

    private static List<AgentMetadata> resolveAgentsToEnsure(UpsertRightsStatementRequest request) {
        if (request == null || request.agents() == null) {
            return List.of();
        }
        var result = new ArrayList<AgentMetadata>();
        for (var grant : request.agents()) {
            if (grant == null) {
                continue;
            }

            var a = grant.agent();
            result.add(AgentMetadata.builder()
                    .id(a.id())
                    .name(a.name())
                    .type(a.type())
                    .identifiers(a.identifiers() == null ? List.of() : List.copyOf(a.identifiers()))
                    .build());
        }
        return result;
    }

}
