package com.deposition.domain.adapter.rights;

import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.PremisSnapshot;
import com.deposition.domain.models.RightsStatementMetadata;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.enums.AgentType;
import com.deposition.domain.models.enums.RightsBasis;
import com.deposition.domain.models.valueobject.AgentIdentifier;
import com.deposition.domain.models.valueobject.RightsGranted;
import com.deposition.domain.models.valueobject.RightsStatementAgentLink;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.rights.UpdateObjectVisibilityInPort;
import com.deposition.domain.port.in.rights.UpdateObjectVisibilityRequest;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
import com.deposition.domain.service.PremisPersistenceService;
import com.deposition.domain.service.XmlUtils;
import com.deposition.domain.service.acl.AccessValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@Validated
@RequiredArgsConstructor
public class UpdateObjectVisibilityAdapter implements UpdateObjectVisibilityInPort {

    private static final String VISIBILITY_RIGHTS_STATEMENT_ID_PREFIX = "visibility_";
    private static final String PUBLIC_AGENT_VALUE = "PUBLIC";

    private final AccessValidatorService accessValidatorService;
    private final FileStorageOutPort fileStorage;
    private final RightsStatementPremisUpdater rightsStatementPremisUpdater;
    private final PremisPersistenceService premisPersistenceService;
    private final PremisSnapshotConverter premisSnapshotConverter;

    @Override
    public DepositionResult updateVisibility(UUID objectId, UpdateObjectVisibilityRequest request) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.visibility() == null) {
            throw new IllegalArgumentException("visibility must not be null");
        }

        accessValidatorService.validateCurrentUserIsSuperAdmin(objectId);

        var premis = loadPremis(objectId);

        var snapshot = premisSnapshotConverter.map(premis);

        var rightsStatement = buildVisibilityRightsStatement(snapshot, objectId, request.visibility());

        var ensurePublicAgent = AgentMetadata.builder()
                .id(null)
                .name(PUBLIC_AGENT_VALUE)
                .type(AgentType.SOFTWARE)
                .identifiers(List.of(AgentIdentifier.builder()
                        .type(AgentIdentifierType.PUBLIC)
                        .value(PUBLIC_AGENT_VALUE)
                        .build()))
                .build();

        rightsStatementPremisUpdater.upsertRightsStatement(
                premis,
                objectId,
                rightsStatement,
                List.of(ensurePublicAgent));

        return premisPersistenceService.persistPremis(objectId, premis);
    }

    private RightsStatementMetadata buildVisibilityRightsStatement(
            PremisSnapshot snapshot,
            UUID objectId,
            UpdateObjectVisibilityRequest.Visibility visibility) {
        if (visibility == null) {
            throw new IllegalArgumentException("visibility must not be null");
        }

        String rightsStatementId = findExistingVisibilityRightsStatementId(snapshot)
                .orElse(VISIBILITY_RIGHTS_STATEMENT_ID_PREFIX + objectId);

        var model = RightsStatementMetadata.builder().build();
        model.setId(rightsStatementId);
        model.setRightsBasis(RightsBasis.OTHER.name());
        model.setRightsGranted(List.of(RightsGranted.builder()
                .act(visibility.name())
                .build()));
        model.setLinkingAgentIdentifiers(List.of(RightsStatementAgentLink.builder()
                .agentIdentifier(new AgentIdentifier(AgentIdentifierType.PUBLIC, PUBLIC_AGENT_VALUE))
                .build()));
        return model;
    }

    private Optional<String> findExistingVisibilityRightsStatementId(PremisSnapshot snapshot) {
        if (snapshot == null || snapshot.getRightsStatements() == null) {
            return Optional.empty();
        }

        return snapshot.getRightsStatements().stream()
                .filter(Objects::nonNull)
                .filter(rs -> rs.getLinkingAgentIdentifiers() != null)
                .filter(rs -> rs.getLinkingAgentIdentifiers().stream()
                        .filter(Objects::nonNull)
                        .map(RightsStatementAgentLink::getAgentIdentifier)
                        .filter(Objects::nonNull)
                        .anyMatch(id -> id.getType() == AgentIdentifierType.PUBLIC
                                && id.getValue() != null
                                && PUBLIC_AGENT_VALUE.equalsIgnoreCase(id.getValue())))
                .map(RightsStatementMetadata::getId)
                .filter(id -> id != null && !id.isBlank())
                .findFirst();
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
