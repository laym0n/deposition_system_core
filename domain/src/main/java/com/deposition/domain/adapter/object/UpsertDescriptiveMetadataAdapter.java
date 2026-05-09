package com.deposition.domain.adapter.object;

import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.port.in.object.UpsertDescriptiveMetadataInPort;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.domain.port.out.ObjectIndexOutPort;
import com.deposition.domain.service.DescriptiveMetadataService;
import com.deposition.domain.service.IntellectualEntityTypeResolver;
import com.deposition.domain.service.acl.AccessValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.UUID;

@Component
@Validated
@RequiredArgsConstructor
public class UpsertDescriptiveMetadataAdapter implements UpsertDescriptiveMetadataInPort {

    private final AccessValidatorService accessValidatorService;
    private final DescriptiveMetadataService descriptiveMetadataService;
    private final ObjectIndexLookupOutPort objectIndexLookupOutPort;
    private final ObjectIndexOutPort objectIndexOutPort;
    private final IntellectualEntityTypeResolver intellectualEntityTypeResolver;

    @Override
    public Map<String, Object> upsertDescriptiveMetadata(UUID objectId,
                                                         String entityTypeName,
                                                         String descriptiveMetadataJson) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (entityTypeName == null || entityTypeName.isBlank()) {
            throw new IllegalArgumentException("entityTypeName must not be blank");
        }
        if (descriptiveMetadataJson == null || descriptiveMetadataJson.isBlank()) {
            throw new IllegalArgumentException("descriptiveMetadataJson must not be blank");
        }

        accessValidatorService.validateCurrentUserHasPermission(objectId, AclPermission.WRITE);

        var existing = objectIndexLookupOutPort.findByObjectId(objectId)
                .orElseThrow(() -> new ResourceNotFoundException("Object", objectId.toString()));

        var entityType = intellectualEntityTypeResolver.resolveByName(entityTypeName);

        var extractedFields = descriptiveMetadataService.validateAndPersistIfPresent(
                objectId,
                entityType,
                descriptiveMetadataJson);

        ObjectIndexDocument updated = new ObjectIndexDocument(
                existing.objectId(),
                existing.intellectualEntityTypeName(),
                existing.acl(),
                existing.anchors(),
                existing.visibility(),
                existing.premis(),
                extractedFields);

        objectIndexOutPort.index(updated);

        return extractedFields;
    }
}
