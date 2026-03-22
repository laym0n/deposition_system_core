package com.deposition.domain.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.deposition.domain.models.IntellectualEntityMetadata;
import com.deposition.domain.models.PremisSnapshot;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ObjectIndexingService {

    private final ObjectIndexOutPort objectIndexOutPort;

    public void indexIntellectualEntity(UUID intellectualEntityId,
            ObjectAcl acl,
            List<ObjectIndexDocument.Anchor> anchors,
            PremisSnapshot snapshot,
            Map<String, Object> intellectualEntityDescriptiveFields) {
        if (intellectualEntityId == null) {
            throw new IllegalArgumentException("intellectualEntityId must not be null");
        }
        if (acl == null) {
            throw new IllegalArgumentException("acl must not be null");
        }

        // Ensure caller-provided ACL is associated with the indexed object.
        if (acl.getObjectId() == null || !intellectualEntityId.equals(acl.getObjectId())) {
            acl = ObjectAcl.builder()
                    .objectId(intellectualEntityId)
                    .entries(acl.getEntries())
                    .build();
        }

        IntellectualEntityMetadata entity = snapshot.getObjects() == null ? null
                : snapshot.getObjects().stream()
                        .filter(IntellectualEntityMetadata.class::isInstance)
                        .map(IntellectualEntityMetadata.class::cast)
                        .filter(ie -> intellectualEntityId.equals(ie.getId()))
                        .findFirst()
                        .orElse(null);

        if (entity == null) {
            entity = snapshot.getObjects() == null ? null
                    : snapshot.getObjects().stream()
                            .filter(IntellectualEntityMetadata.class::isInstance)
                            .map(IntellectualEntityMetadata.class::cast)
                            .findFirst()
                            .orElse(null);
        }

        if (entity == null) {
            return;
        }

        ObjectIndexDocument doc = new ObjectIndexDocument(
                intellectualEntityId,
                "INTELLECTUAL_ENTITY",
                acl,
                entity.getOriginalName(),
                anchors,
                entity.getIdentifiers() == null ? List.of() : entity.getIdentifiers(),
                entity.getRelationships() == null ? List.of() : entity.getRelationships(),
                intellectualEntityDescriptiveFields == null || intellectualEntityDescriptiveFields.isEmpty()
                ? null
                : intellectualEntityDescriptiveFields);

        objectIndexOutPort.index(doc);
    }
}
