package com.deposition.domain.adapter.event;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.enums.*;
import com.deposition.domain.models.valueobject.*;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.event.RecordObjectEventInPort;
import com.deposition.domain.port.in.event.RecordObjectEventRequest;
import com.deposition.domain.port.out.*;
import com.deposition.domain.service.ResourceHashCalculatorUtils;
import com.deposition.domain.service.StatisticsEventReporter;
import com.deposition.domain.service.XmlUtils;
import com.deposition.domain.service.acl.AccessValidatorService;
import com.deposition.domain.port.out.UserOutPort;
import com.deposition.domain.models.statistics.StatisticsEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Validated
@RequiredArgsConstructor
public class RecordObjectEventAdapter implements RecordObjectEventInPort {

    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;
    private final AccessValidatorService accessValidatorService;
    private final com.deposition.domain.dto.schema.premis.v3.converter.EventConverter eventConverter;
    private final com.deposition.domain.dto.schema.premis.v3.converter.AgentConverter agentConverter;
    private final ObjectIndexLookupOutPort objectIndexLookupOutPort;
    private final ObjectIndexOutPort objectIndexOutPort;
    private final StatisticsEventReporter statisticsEventReporter;
    private final UserOutPort userOutPort;

    private static List<EventAgentLink> buildAgentLinks(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        String userId = authentication.getName();
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        var agentIdentifier = new AgentIdentifier(AgentIdentifierType.SYSTEM, userId);
        return List.of(new EventAgentLink(agentIdentifier, List.of(EventAgentLinkRole.AUTHORIZER)));
    }

    private static AnchorRecord buildAnchorRecord(UUID objectId, String versionId, Resource premisMetadata) {
        String algorithm = ResourceHashCalculatorUtils.DEFAULT_HASH_ALGORITHM;
        var premisMetadataHash = ResourceHashCalculatorUtils.calculateHash(premisMetadata, algorithm);
        return AnchorRecord.builder()
                .objectId(objectId.toString())
                .versionId(versionId)
                .hash(premisMetadataHash)
                .hashAlgorithm(algorithm)
                .build();
    }

    @Override
    public DepositionResult recordEvent(UUID objectId, RecordObjectEventRequest request) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        accessValidatorService.validateCurrentUserHasPermission(objectId, AclPermission.WRITE);

        Resource premisXml;
        try {
            premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Object", objectId.toString());
        }

        var premis = XmlUtils.parsePremis(premisXml);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        var eventId = UUID.randomUUID();

        var event = EventMetadata.builder()
                .id(eventId)
                .identifier(new EventIdentifier(EventIdentifierType.SYSTEM, eventId.toString()))
                .type(request.type())
                .dateTime(OffsetDateTime.now())
                .detail(request.detail())
                .outcome(request.outcome())
                .objectLinks(List.of(new EventObjectLink(
                        new ObjectIdentifier(ObjectIdentifierType.SYSTEM, objectId.toString()),
                        List.of(EventObjectLinkRole.OUTCOME))))
                .agentLinks(buildAgentLinks(authentication))
                .build();

        premis.getEvent().add(eventConverter.map(event));
        ensureCurrentUserAgentPresent(premis, authentication);

        var updatedPremisResource = XmlUtils.createXmlResource(premis, "deposition-metadata");
        var premisStorage = fileStorage.persist(updatedPremisResource, objectId.toString());

        var anchorRecord = buildAnchorRecord(objectId, premisStorage.getVersionId(), updatedPremisResource);
        var txId = blockchain.persistAnchorRecord(anchorRecord);

        updateObjectIndex(objectId, premisStorage.getVersionId(), txId);

        // Adding an Event to PREMIS creates a new object version.
        userOutPort.getOptinalCurrentUserId()
                .ifPresent(userId -> statisticsEventReporter.report(
                        StatisticsEventType.OBJECT_VERSION_CREATE,
                        objectId,
                        premisStorage.getVersionId(),
                        userId));

        return new DepositionResult(objectId, txId, premisStorage.getVersionId());
    }

    private void updateObjectIndex(UUID objectId, String versionId, String txId) {
        var existing = objectIndexLookupOutPort.findByObjectId(objectId)
                .orElseThrow(() -> new ResourceNotFoundException("Object", objectId.toString()));

        List<ObjectIndexDocument.Anchor> anchors = List.of(new ObjectIndexDocument.Anchor(versionId, txId, null));

        var updated = new ObjectIndexDocument(
                existing.objectId(),
                existing.intellectualEntityType(),
                existing.acl(),
                anchors,
                existing.visibility(),
                existing.premis(),
                existing.descriptive());

        objectIndexOutPort.index(updated);
    }

    private void ensureCurrentUserAgentPresent(
            com.deposition.domain.dto.schema.premis.v3.PremisComplexType premis,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        String agentId = authentication.getName();
        if (agentId == null || agentId.isBlank()) {
            return;
        }

        var agentXmlId = "id_" + agentId;
        if (premis.getAgent() != null && premis.getAgent().stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(agent -> java.util.Objects.equals(agentXmlId, agent.getXmlID()))) {
            return;
        }

        var agent = com.deposition.domain.models.AgentMetadata.builder()
                .id(agentId)
                .name(agentId)
                .type(com.deposition.domain.models.enums.AgentType.PERSON)
                .identifiers(List.of(AgentIdentifier.builder()
                        .type(AgentIdentifierType.SYSTEM)
                        .value(agentId)
                        .build()))
                .build();

        premis.getAgent().add(agentConverter.map(agent));
    }
}
