package com.deposition.domain.adapter.object;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.deposition.domain.adapter.converter.FileParamConverter;
import com.deposition.domain.adapter.converter.IntellectualEntityParamConverter;
import com.deposition.domain.adapter.converter.RepresentationParamConverter;
import com.deposition.domain.dto.schema.premis.v3.EventComplexType;
import com.deposition.domain.dto.schema.premis.v3.File;
import com.deposition.domain.dto.schema.premis.v3.IntellectualEntity;
import com.deposition.domain.dto.schema.premis.v3.ObjectComplexType;
import com.deposition.domain.dto.schema.premis.v3.ObjectIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.Representation;
import com.deposition.domain.dto.schema.premis.v3.converter.AgentConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.EventConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.FileMetadataConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.IntellectualEntityConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisSnapshotConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.RepresentationMetadataConverter;
import com.deposition.domain.exception.ObjectNotFoundException;
import com.deposition.domain.models.EventMetadata;
import com.deposition.domain.models.FileMetadata;
import com.deposition.domain.models.IntellectualEntityMetadata;
import com.deposition.domain.models.PremisSnapshot;
import com.deposition.domain.models.RepresentationMetadata;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.enums.EventAgentLinkRole;
import com.deposition.domain.models.enums.EventIdentifierType;
import com.deposition.domain.models.enums.EventObjectLinkRole;
import com.deposition.domain.models.enums.EventType;
import com.deposition.domain.models.enums.ObjectIdentifierType;
import com.deposition.domain.models.valueobject.AgentIdentifier;
import com.deposition.domain.models.valueobject.EventAgentLink;
import com.deposition.domain.models.valueobject.EventDetailInformation;
import com.deposition.domain.models.valueobject.EventIdentifier;
import com.deposition.domain.models.valueobject.EventObjectLink;
import com.deposition.domain.models.valueobject.Identifier;
import com.deposition.domain.models.valueobject.ObjectIdentifier;
import com.deposition.domain.port.in.FileMetadataParam;
import com.deposition.domain.port.in.IntellectualEntityMetadataParam;
import com.deposition.domain.port.in.RepresentationMetadataParam;
import com.deposition.domain.port.in.UpdateFileMetadataParam;
import com.deposition.domain.port.in.UpdateMetadataParams;
import com.deposition.domain.port.in.UpdateRepresentationMetadataParam;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
final class PremisMetadataUpdater {

    private final PremisSnapshotConverter snapshotConverter;
    private final IntellectualEntityConverter intellectualEntityConverter;
    private final RepresentationMetadataConverter representationMetadataConverter;
    private final FileMetadataConverter fileMetadataConverter;
    private final EventConverter eventConverter;
    private final AgentConverter agentConverter;

    private final IntellectualEntityParamConverter intellectualEntityParamConverter;
    private final RepresentationParamConverter representationParamConverter;
    private final FileParamConverter fileParamConverter;

    public UpdateResult applyUpdate(PremisComplexType premis,
            UUID objectId,
            UpdateMetadataParams params) {
        if (premis == null) {
            throw new IllegalArgumentException("premis must not be null");
        }
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (params == null) {
            return new UpdateResult(premis, false);
        }

        var snapshot = snapshotConverter.map(premis);
        boolean updated = false;

        updated |= applyIntellectualEntityUpdate(premis, snapshot, objectId, params.intellectualEntityMetadata());

        if (params.representations() != null) {
            for (var representationPatch : params.representations()) {
                if (representationPatch == null) {
                    continue;
                }
                updated |= applyRepresentationUpdate(premis, snapshot, representationPatch);
            }
        }

        if (updated) {
            addMetadataModificationEvent(premis, snapshot, objectId);
        }

        return new UpdateResult(premis, updated);
    }

    private boolean applyIntellectualEntityUpdate(
            PremisComplexType premis,
            PremisSnapshot snapshot,
            UUID objectId,
            IntellectualEntityMetadataParam patch) {
        if (patch == null) {
            return false;
        }

        var entity = findObjectById(snapshot, objectId, IntellectualEntityMetadata.class);
        if (!hasAnyValue(patch)) {
            return false;
        }

        intellectualEntityParamConverter.update(entity, patch);

        var updatedPremisObject = intellectualEntityConverter.map(entity);
        replacePremisObject(premis, objectId, updatedPremisObject);
        return true;
    }

    private boolean applyRepresentationUpdate(
            PremisComplexType premis,
            PremisSnapshot snapshot,
            UpdateRepresentationMetadataParam patch) {
        boolean updated = false;

        var representationId = patch.representationId();

        if (patch.representationMetadata() != null) {
            var representation = findObjectById(snapshot, representationId, RepresentationMetadata.class);
            updated |= applyRepresentationMetadata(representation, patch.representationMetadata());

            if (updated) {
                var updatedPremisObject = representationMetadataConverter.map(representation);
                replacePremisObject(premis, representationId, updatedPremisObject);
            }
        }

        if (patch.files() != null) {
            for (var filePatch : patch.files()) {
                if (filePatch == null) {
                    continue;
                }
                updated |= applyFileUpdate(premis, snapshot, filePatch);
            }
        }

        return updated;
    }

    private boolean applyRepresentationMetadata(RepresentationMetadata target, RepresentationMetadataParam patch) {
        if (patch == null) {
            return false;
        }

        if (!hasAnyValue(patch)) {
            return false;
        }

        representationParamConverter.update(target, patch);
        return true;
    }

    private boolean applyFileUpdate(
            PremisComplexType premis,
            PremisSnapshot snapshot,
            UpdateFileMetadataParam patch) {
        if (patch.fileId() == null || patch.fileMetadata() == null) {
            return false;
        }

        var fileId = patch.fileId();
        var file = findObjectById(snapshot, fileId, FileMetadata.class);

        boolean changed = applyFileMetadata(file, patch.fileMetadata());
        if (!changed) {
            return false;
        }

        var updatedPremisObject = fileMetadataConverter.map(file);
        replacePremisObject(premis, fileId, updatedPremisObject);
        return true;
    }

    private boolean applyFileMetadata(FileMetadata target, FileMetadataParam patch) {
        if (patch == null) {
            return false;
        }

        if (!hasAnyValue(patch)) {
            return false;
        }

        fileParamConverter.update(target, patch);
        return true;
    }

    private static boolean hasAnyValue(IntellectualEntityMetadataParam patch) {
        return patch.originalName() != null
                || patch.identifiers() != null
                || patch.relationships() != null;
    }

    private static boolean hasAnyValue(RepresentationMetadataParam patch) {
        return patch.originalName() != null;
    }

    private static boolean hasAnyValue(FileMetadataParam patch) {
        return patch.originalName() != null;
    }

    private void replacePremisObject(PremisComplexType premis,
            UUID objectId,
            ObjectComplexType replacement) {
        if (replacement == null) {
            throw new IllegalStateException("Replacement PREMIS object is null for objectId=" + objectId);
        }

        var objects = premis.getObject();
        for (int i = 0; i < objects.size(); i++) {
            var obj = objects.get(i);
            UUID id = extractLocalId(obj);
            if (Objects.equals(id, objectId)) {
                objects.set(i, replacement);
                return;
            }
        }

        throw new ObjectNotFoundException(objectId);
    }

    private static UUID extractLocalId(ObjectComplexType obj) {
        if (obj == null) {
            return null;
        }
        try {
            if (obj instanceof IntellectualEntity ie) {
                return extractLocalIdFromIdentifiers(ie.getObjectIdentifier());
            }
            if (obj instanceof Representation rep) {
                return extractLocalIdFromIdentifiers(rep.getObjectIdentifier());
            }
            if (obj instanceof File file) {
                return extractLocalIdFromIdentifiers(file.getObjectIdentifier());
            }
        } catch (RuntimeException ex) {
            // ignore and return null
        }
        return null;
    }

    private static UUID extractLocalIdFromIdentifiers(
            List<ObjectIdentifierComplexType> identifiers) {
        if (identifiers == null) {
            return null;
        }
        for (var id : identifiers) {
            if (id == null || id.getObjectIdentifierType() == null) {
                continue;
            }
            var type = id.getObjectIdentifierType().getValue();
            if (!Objects.equals(ObjectIdentifierType.LOCAL.name(), type)) {
                continue;
            }
            var value = id.getObjectIdentifierValue();
            if (value == null || value.isBlank()) {
                continue;
            }
            return UUID.fromString(value);
        }
        return null;
    }

    private <T> T findObjectById(PremisSnapshot snapshot, UUID objectId, Class<T> type) {
        if (snapshot == null || snapshot.getObjects() == null) {
            throw new ObjectNotFoundException(objectId);
        }

        for (var obj : snapshot.getObjects()) {
            if (obj == null || obj.getId() == null) {
                continue;
            }
            if (!Objects.equals(obj.getId(), objectId)) {
                continue;
            }

            if (!type.isInstance(obj)) {
                throw new IllegalArgumentException("Object type mismatch for objectId=" + objectId
                        + ", expected=" + type.getSimpleName()
                        + ", actual=" + obj.getClass().getSimpleName());
            }
            return type.cast(obj);
        }

        throw new ObjectNotFoundException(objectId);
    }

    private void addMetadataModificationEvent(
            PremisComplexType premis,
            PremisSnapshot snapshot,
            UUID objectId) {
        var eventId = UUID.randomUUID();
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        var event = EventMetadata.builder()
                .id(eventId)
                .identifier(new EventIdentifier(EventIdentifierType.LOCAL, eventId.toString()))
                .type(EventType.METADATA_MODIFICATION)
                .dateTime(OffsetDateTime.now())
                .detail(List.of(new EventDetailInformation("Metadata updated via REST API")))
                .objectLinks(List.of(new EventObjectLink(
                        new ObjectIdentifier(ObjectIdentifierType.LOCAL, objectId.toString()),
                        List.of(EventObjectLinkRole.OUTCOME))))
                .agentLinks(buildAgentLinks(authentication))
                .build();

        EventComplexType premisEvent = eventConverter.map(event);
        premis.getEvent().add(premisEvent);

        ensureAgentPresent(premis, snapshot, authentication);
    }

    private static List<EventAgentLink> buildAgentLinks(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        var agentIdentifier = new AgentIdentifier(AgentIdentifierType.LOCAL, authentication.getName());
        return List.of(new EventAgentLink(agentIdentifier, List.of(EventAgentLinkRole.AUTHORIZER)));
    }

    private void ensureAgentPresent(PremisComplexType premis,
            PremisSnapshot snapshot,
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
                .filter(Objects::nonNull)
                .anyMatch(agent -> Objects.equals(agentXmlId, agent.getXmlID()))) {
            return;
        }

        // Reuse the same strategy as PremisMetadataBuilder: represent current user as an agent.
        var agent = com.deposition.domain.models.AgentMetadata.builder()
                .id(agentId)
                .name(agentId)
                .type(com.deposition.domain.models.enums.AgentType.PERSON)
                .identifiers(List.of(Identifier.builder()
                        .type(ObjectIdentifierType.LOCAL.name())
                        .value(agentId)
                        .build()))
                .build();

        premis.getAgent().add(agentConverter.map(agent));
    }

    record UpdateResult(PremisComplexType premis, boolean updated) {

    }
}
