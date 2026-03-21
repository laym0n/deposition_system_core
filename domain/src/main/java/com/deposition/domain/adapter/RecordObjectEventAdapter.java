package com.deposition.domain.adapter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.ObjectNotFoundException;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.enums.EventAgentLinkRole;
import com.deposition.domain.models.enums.EventIdentifierType;
import com.deposition.domain.models.enums.EventObjectLinkRole;
import com.deposition.domain.models.enums.ObjectIdentifierType;
import com.deposition.domain.models.valueobject.AgentIdentifier;
import com.deposition.domain.models.valueobject.EventAgentLink;
import com.deposition.domain.models.valueobject.EventDetailInformation;
import com.deposition.domain.models.valueobject.EventIdentifier;
import com.deposition.domain.models.valueobject.EventObjectLink;
import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.port.in.RecordObjectEventInPort;
import com.deposition.domain.port.in.dto.DepositionResult;
import com.deposition.domain.port.in.dto.RecordObjectEventRequest;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.domain.port.out.ObjectIndexOutPort;

import lombok.RequiredArgsConstructor;

@Component
@Validated
@RequiredArgsConstructor
public class RecordObjectEventAdapter implements RecordObjectEventInPort {

    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;
    private final PremisOwnershipValidator premisOwnershipValidator;
    private final com.deposition.domain.dto.schema.premis.v3.converter.EventConverter eventConverter;
    private final com.deposition.domain.dto.schema.premis.v3.converter.AgentConverter agentConverter;
    private final ObjectIndexLookupOutPort objectIndexLookupOutPort;
    private final ObjectIndexOutPort objectIndexOutPort;

    @Override
    public DepositionResult recordEvent(UUID objectId, RecordObjectEventRequest request) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        premisOwnershipValidator.validateCurrentUserHasPermission(objectId, AclPermission.WRITE);

        Resource premisXml;
        try {
            premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
        } catch (IllegalArgumentException ex) {
            throw new ObjectNotFoundException(objectId);
        }

        var premis = XmlUtils.parsePremis(premisXml);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        var eventId = UUID.randomUUID();

        var event = EventMetadata.builder()
                .id(eventId)
                .identifier(new EventIdentifier(EventIdentifierType.LOCAL, eventId.toString()))
                .type(request.type())
                .dateTime(OffsetDateTime.now())
                .detail(List.of(new EventDetailInformation(request.detail())))
                .objectLinks(List.of(new EventObjectLink(
                        new ObjectIdentifier(ObjectIdentifierType.LOCAL, objectId.toString()),
                        List.of(EventObjectLinkRole.OUTCOME))))
                .agentLinks(buildAgentLinks(authentication))
                .build();

        premis.getEvent().add(eventConverter.map(event));
        ensureCurrentUserAgentPresent(premis, authentication);

        var updatedPremisResource = XmlUtils.createXmlResource(premis, "deposition-metadata");
        var premisStorage = fileStorage.persist(updatedPremisResource, objectId.toString());

        var anchorRecord = buildAnchorRecord(updatedPremisResource);
        anchorRecord = blockchain.persistAnchorRecord(anchorRecord);

        updateObjectIndex(objectId, premisStorage.getVersionId(), anchorRecord.getTxId());

        return new DepositionResult(objectId, anchorRecord.getTxId(), premisStorage.getVersionId());
    }

    private void updateObjectIndex(UUID objectId, String versionId, String txId) {
        var existing = objectIndexLookupOutPort.findByObjectId(objectId)
                .orElseThrow(() -> new ObjectNotFoundException(objectId));

        List<ObjectIndexDocument.Anchor> anchors = List.of(new ObjectIndexDocument.Anchor(versionId, txId, null));

        var updated = new ObjectIndexDocument(
                existing.objectId(),
                existing.entityType(),
                existing.acl(),
                existing.originalName(),
                anchors,
                existing.identifiers(),
                existing.relationships(),
                existing.descriptive());

        objectIndexOutPort.index(updated);
    }

    private static List<EventAgentLink> buildAgentLinks(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        String userId = authentication.getName();
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        var agentIdentifier = new AgentIdentifier(AgentIdentifierType.LOCAL, userId);
        return List.of(new EventAgentLink(agentIdentifier, List.of(EventAgentLinkRole.AUTHORIZER)));
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
                .identifiers(List.of(com.deposition.domain.models.valueobject.Identifier.builder()
                        .type(ObjectIdentifierType.LOCAL.name())
                        .value(agentId)
                        .build()))
                .build();

        premis.getAgent().add(agentConverter.map(agent));
    }

    private static AnchorRecord buildAnchorRecord(Resource premisMetadata) {
        var premisMetadataHash = ResourceHashCalculator.sha256(premisMetadata);
        return AnchorRecord.builder()
                .premisMetadataHash(premisMetadataHash)
                .build();
    }
}
