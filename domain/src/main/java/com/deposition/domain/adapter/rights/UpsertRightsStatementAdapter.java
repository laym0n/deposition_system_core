package com.deposition.domain.adapter.rights;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.RightsStatementMetadata;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.valueobject.RightsStatementAgentLink;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.rights.UpsertRightsStatementInPort;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.service.PremisPersistenceService;
import com.deposition.domain.service.XmlUtils;
import com.deposition.domain.service.acl.AccessValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Component
@Validated
@RequiredArgsConstructor
public class UpsertRightsStatementAdapter implements UpsertRightsStatementInPort {

    private final FileStorageOutPort fileStorage;
    private final RightsStatementPremisUpdater rightsStatementPremisUpdater;
    private final AccessValidatorService accessValidatorService;
    private final PremisPersistenceService premisPersistenceService;

    private static RightsStatementMetadata toRightsStatementMetadata(UpsertRightsStatementRequest request) {
        if (request == null) {
            return null;
        }

        var model = RightsStatementMetadata.builder().build();
        model.setId(request.rightsStatementId());
        model.setRightsBasis(request.rightsBasis().name());

        if (request.payload() != null) {
            var payload = request.payload();
            if (payload.copyrightInformation() != null) {
                model.setCopyrightInformation(List.of(payload.copyrightInformation()));
            }
            if (payload.licenseInformation() != null) {
                model.setLicenseInformation(List.of(payload.licenseInformation()));
            }
            if (payload.otherRightsInformation() != null) {
                model.setOtherRightsInformation(payload.otherRightsInformation());
            }
            if (payload.statuteInformation() != null) {
                model.setStatuteInformation(new ArrayList<>(payload.statuteInformation()));
            }
            if (payload.rightsGranted() != null) {
                model.setRightsGranted(new ArrayList<>(payload.rightsGranted()));
            }
        }

        // linkingAgentIdentifiers: build from full set of identifiers for each agent grant
        if (request.agents() != null) {
            var links = new ArrayList<RightsStatementAgentLink>();
            for (var grant : request.agents()) {
                if (grant == null || grant.agent() == null || grant.agent().identifiers() == null) {
                    continue;
                }
                for (var id : grant.agent().identifiers()) {
                    if (id == null || id.getType() == null || id.getValue() == null || id.getValue().isBlank()) {
                        continue;
                    }
                    // ignore SYSTEM in external payload (reserved for internal agents)
                    if (id.getType() == AgentIdentifierType.SYSTEM) {
                        continue;
                    }
                    var roles = new LinkedHashSet<String>();
                    if (grant.linkingAgentRoles() != null) {
                        for (var r : grant.linkingAgentRoles()) {
                            if (r != null && !r.isBlank()) {
                                roles.add(r);
                            }
                        }
                    }
                    links.add(RightsStatementAgentLink.builder()
                            .agentIdentifier(id)
                            .roles(roles)
                            .build());
                }
            }
            model.setLinkingAgentIdentifiers(List.copyOf(links));
        }

        return model;
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
					.id(null)
                    .name(a.name())
                    .type(a.type())
                    .identifiers(a.identifiers() == null ? List.of() : List.copyOf(a.identifiers()))
                    .build());
        }
        return result;
    }

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
        var rightsStatement = toRightsStatementMetadata(request);
        if (rightsStatement == null) {
            throw new IllegalArgumentException("Unable to map request to RightsStatementMetadata");
        }
        rightsStatementPremisUpdater.upsertRightsStatement(premis, objectId, rightsStatement, ensureAgents);

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

}
