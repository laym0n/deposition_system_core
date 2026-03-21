package com.deposition.domain.adapter.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.deposition.domain.adapter.converter.IntellectualEntityParamConverter;
import com.deposition.domain.dto.schema.premis.v3.IntellectualEntity;
import com.deposition.domain.dto.schema.premis.v3.converter.IntellectualEntityConverter;
import com.deposition.domain.models.IntellectualEntityMetadata;
import com.deposition.domain.models.enums.ObjectIdentifierType;
import com.deposition.domain.models.enums.ObjectRelationshipSubType;
import com.deposition.domain.models.enums.ObjectRelationshipType;
import com.deposition.domain.models.valueobject.RelationObjectIdentifier;
import com.deposition.domain.models.valueobject.Relationship;
import com.deposition.domain.port.in.object.IntellectualEntityMetadataParam;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
final class IntellectualEntityMetadataBuilder {

    private final IntellectualEntityConverter intellectualEntityConverter;
    private final IntellectualEntityParamConverter intellectualEntityParamConverter;
    private final CommonMetadataBuilder commonMetadataBuilder;

    public CommonMetadataBuilder.MetadataStructure buildForIntellectualEntity(
            IntellectualEntityMetadataParam baseMetadata,
            List<UUID> representationObjectIds, UUID intellectualEntityId,
            Authentication authentication) {

        var intellectualEntityMetadata = buildIntellectualEntityObject(baseMetadata, intellectualEntityId,
                representationObjectIds);

        return commonMetadataBuilder.toMetadataStructure(intellectualEntityId, intellectualEntityMetadata, authentication);
    }

    private IntellectualEntity buildIntellectualEntityObject(IntellectualEntityMetadataParam baseMetadata,
            UUID objectId, List<UUID> representationObjectIds) {
        var intellectualEntity = IntellectualEntityMetadata.builder()
                .id(objectId)
                .relationships(new ArrayList<>(List.of(
                        Relationship.builder()
                                .type(ObjectRelationshipType.STRUCTURAL)
                                .subType(ObjectRelationshipSubType.IS_REPRESENTED_BY)
                                .relatedObjects(
                                        representationObjectIds.stream()
                                                .map(representationObjectId -> {
                                                    return (RelationObjectIdentifier) RelationObjectIdentifier
                                                            .builder()
                                                            .type(ObjectIdentifierType.LOCAL)
                                                            .value(representationObjectId
                                                                    .toString())
                                                            .build();
                                                })
                                                .toList())
                                .build())))
                .build();
        intellectualEntityParamConverter.update(intellectualEntity, baseMetadata);
        return intellectualEntityConverter.map(intellectualEntity);
    }
}
