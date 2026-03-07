package com.deposition.domain.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.deposition.domain.models.IntellectualEntityMetadata;
import com.deposition.domain.models.PremisSnapshot;
import com.deposition.domain.models.acl.AclEntry;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.AclPrincipal;
import com.deposition.domain.models.acl.AclPrincipalType;
import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ObjectIndexingService {

    private final ObjectIndexOutPort objectIndexOutPort;

    public void indexIntellectualEntity(UUID intellectualEntityId,
            String ownerUserId,
            String blockchainTxId,
            PremisSnapshot snapshot,
            Map<String, Object> intellectualEntityDescriptiveFields) {
        ObjectAcl acl = buildDefaultAcl(intellectualEntityId, ownerUserId);

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
                blockchainTxId,
                entity.getIdentifiers() == null ? List.of() : entity.getIdentifiers(),
                entity.getRelationships() == null ? List.of() : entity.getRelationships(),
                intellectualEntityDescriptiveFields == null || intellectualEntityDescriptiveFields.isEmpty()
                ? null
                : intellectualEntityDescriptiveFields);

        objectIndexOutPort.index(doc);
    }

    private static ObjectAcl buildDefaultAcl(UUID objectId, String ownerUserId) {
        return ObjectAcl.builder()
                .objectId(objectId)
                .entries(List.of(
                        AclEntry.builder()
                                .principal(AclPrincipal.builder().type(AclPrincipalType.USER).id(ownerUserId).build())
                                .permissions(java.util.EnumSet.of(AclPermission.READ, AclPermission.WRITE))
                                .build()))
                .build();
    }
}
