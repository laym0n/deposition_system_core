package com.deposition.domain.service;

import com.deposition.domain.models.IntellectualEntityMetadata;
import com.deposition.domain.models.PremisSnapshot;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexOutPort;
import com.deposition.domain.port.out.PremisIndexFields;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ObjectIndexingService {

    private final ObjectIndexOutPort objectIndexOutPort;

    public void indexIntellectualEntity(UUID intellectualEntityId,
                                        String intellectualEntityTypeName,
                                        ObjectAcl acl,
                                        List<ObjectIndexDocument.Anchor> anchors,
                                        PremisSnapshot snapshot,
                                        Map<String, Object> intellectualEntityDescriptiveFields) {
        if (intellectualEntityId == null) {
            throw new IllegalArgumentException("intellectualEntityId must not be null");
        }
        if (intellectualEntityTypeName == null || intellectualEntityTypeName.isBlank()) {
            throw new IllegalArgumentException("intellectualEntityTypeName must not be blank");
        }
        if (acl == null) {
            throw new IllegalArgumentException("acl must not be null");
        }

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

        ObjectIndexDocument.Visibility visibility = ObjectVisibilityResolver.resolve(snapshot);

        var premis = new PremisIndexFields(
                intellectualEntityId,
                entity.getOriginalName(),
                entity.getIdentifiers() == null ? List.of() : entity.getIdentifiers(),
                entity.getRelationships() == null ? List.of() : entity.getRelationships());

        ObjectIndexDocument doc = new ObjectIndexDocument(
                intellectualEntityId,
                intellectualEntityTypeName,
                acl,
                anchors,
                visibility,
                premis,
                intellectualEntityDescriptiveFields == null || intellectualEntityDescriptiveFields.isEmpty()
                        ? null
                        : intellectualEntityDescriptiveFields);

        objectIndexOutPort.index(doc);
    }
}
