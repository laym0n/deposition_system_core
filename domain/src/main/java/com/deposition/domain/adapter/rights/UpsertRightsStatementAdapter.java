package com.deposition.domain.adapter.rights;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.AnchorRecord;
import com.deposition.domain.models.acl.AclEntry;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.AclPrincipal;
import com.deposition.domain.models.acl.AclPrincipalType;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.models.enums.ObjectIdentifierType;
import com.deposition.domain.port.in.rights.UpsertRightsStatementInPort;
import com.deposition.domain.port.in.rights.UpsertRightsStatementRequest;
import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.out.BlockchainOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.domain.port.out.ObjectIndexOutPort;
import com.deposition.domain.service.ResourceHashCalculatorUtils;
import com.deposition.domain.service.XmlUtils;
import com.deposition.domain.service.acl.AccessValidatorService;

import lombok.RequiredArgsConstructor;

@Component
@Validated
@RequiredArgsConstructor
public class UpsertRightsStatementAdapter implements UpsertRightsStatementInPort {

    private final FileStorageOutPort fileStorage;
    private final BlockchainOutPort blockchain;
    private final RightsStatementPremisUpdater rightsStatementPremisUpdater;
    private final AccessValidatorService accessValidatorService;
    private final ObjectIndexLookupOutPort objectIndexLookupOutPort;
    private final ObjectIndexOutPort objectIndexOutPort;

    @Override
    public DepositionResult upsertRightsStatement(UUID objectId, UpsertRightsStatementRequest request) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        // Only owner/editor can update rights.
        accessValidatorService.validateCurrentUserHasPermission(objectId, AclPermission.WRITE);

        Resource premisXml;
        try {
            premisXml = fileStorage.loadPremisMetadataByObjectId(objectId);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Object", objectId.toString());
        }

        var premis = XmlUtils.parsePremis(premisXml);

        var ensureAgents = resolveAgentsToEnsure(request);
        rightsStatementPremisUpdater.upsertRightsStatement(premis, objectId, request, ensureAgents);

        var updatedPremisResource = XmlUtils.createXmlResource(premis, "deposition-metadata");
        var premisStorage = fileStorage.persist(updatedPremisResource, objectId.toString());

        var anchorRecord = buildAnchorRecord(objectId, premisStorage.getVersionId(), updatedPremisResource);
        var txId = blockchain.persistAnchorRecord(anchorRecord);

        // Update OpenSearch: anchors + ACL adjustments (OpenSearch is a source of truth).
        updateObjectIndex(objectId, request, premisStorage.getVersionId(), txId);

        return new DepositionResult(objectId, txId, premisStorage.getVersionId());
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

    private void updateObjectIndex(UUID objectId, UpsertRightsStatementRequest request, String versionId, String txId) {
        var existing = objectIndexLookupOutPort.findByObjectId(objectId)
                .orElseThrow(() -> new ResourceNotFoundException("Object", objectId.toString()));

        ObjectAcl updatedAcl = mergeAcl(existing.acl(), request);

        List<ObjectIndexDocument.Anchor> anchors = List.of(new ObjectIndexDocument.Anchor(versionId, txId, null));

        var updated = new ObjectIndexDocument(
                existing.objectId(),
                existing.entityType(),
                updatedAcl,
                existing.originalName(),
                anchors,
                existing.identifiers(),
                existing.relationships(),
                existing.descriptive());

        objectIndexOutPort.index(updated);
    }

    private static ObjectAcl mergeAcl(ObjectAcl existingAcl, UpsertRightsStatementRequest request) {
        if (existingAcl == null) {
            existingAcl = ObjectAcl.builder().entries(List.of()).build();
        }
        var entries = new ArrayList<AclEntry>();
        if (existingAcl.getEntries() != null) {
            entries.addAll(existingAcl.getEntries().stream().filter(Objects::nonNull).toList());
        }

        if (request.agents() != null) {
            for (var grant : request.agents()) {
                if (grant == null || grant.userId() == null || grant.userId().isBlank()) {
                    continue;
                }

                EnumSet<AclPermission> perms = EnumSet.noneOf(AclPermission.class);
                if (grant.permissions() != null) {
                    for (var p : grant.permissions()) {
                        if (p == null || p.isBlank()) {
                            continue;
                        }
                        try {
                            perms.add(AclPermission.valueOf(p.trim().toUpperCase()));
                        } catch (IllegalArgumentException ex) {
                            // ignore unknown permissions for now
                        }
                    }
                }
                if (perms.isEmpty()) {
                    continue;
                }

                entries = upsertUserEntry(entries, grant.userId(), perms);
            }
        }

        return ObjectAcl.builder()
                .objectId(existingAcl.getObjectId())
                .entries(entries)
                .build();
    }

    private static ArrayList<AclEntry> upsertUserEntry(List<AclEntry> existing, String userId,
            EnumSet<AclPermission> additional) {
        var result = new ArrayList<AclEntry>();
        boolean found = false;
        for (var e : existing) {
            if (e == null) {
                continue;
            }
            if (e.getPrincipal() != null
                    && e.getPrincipal().getType() == AclPrincipalType.USER
                    && Objects.equals(userId, e.getPrincipal().getId())) {
                found = true;
                EnumSet<AclPermission> merged = e.getPermissions() == null
                        ? EnumSet.noneOf(AclPermission.class)
                        : EnumSet.copyOf(e.getPermissions());
                merged.addAll(additional);
                result.add(AclEntry.builder()
                        .principal(AclPrincipal.builder().type(AclPrincipalType.USER).id(userId).build())
                        .permissions(merged)
                        .build());
            } else {
                result.add(e);
            }
        }
        if (!found) {
            result.add(AclEntry.builder()
                    .principal(AclPrincipal.builder().type(AclPrincipalType.USER).id(userId).build())
                    .permissions(additional)
                    .build());
        }
        return result;
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

            if (grant.userId() != null && !grant.userId().isBlank()) {
                // Represent system user as an agent as well.
                result.add(AgentMetadata.builder()
                        .id(grant.userId())
                        .name(grant.userId())
                        .type(com.deposition.domain.models.enums.AgentType.PERSON)
                        .identifiers(List.of(com.deposition.domain.models.valueobject.Identifier.builder()
                                .type(ObjectIdentifierType.SYSTEM.name())
                                .value(grant.userId())
                                .build()))
                        .build());
                continue;
            }

            if (grant.agent() != null) {
                var a = grant.agent();
                result.add(AgentMetadata.builder()
                        .id(a.id())
                        .name(a.name())
                        .type(a.type())
                        .identifiers(a.identifiers() == null ? List.of() : List.copyOf(a.identifiers()))
                        .build());
            }
        }
        return result;
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
}
