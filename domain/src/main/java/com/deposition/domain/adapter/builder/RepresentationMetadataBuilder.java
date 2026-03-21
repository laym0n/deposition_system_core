package com.deposition.domain.adapter.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.deposition.domain.adapter.converter.RepresentationParamConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.RepresentationMetadataConverter;
import com.deposition.domain.models.RepresentationMetadata;
import com.deposition.domain.models.enums.ObjectIdentifierType;
import com.deposition.domain.models.enums.ObjectRelationshipSubType;
import com.deposition.domain.models.enums.ObjectRelationshipType;
import com.deposition.domain.models.valueobject.RelationObjectIdentifier;
import com.deposition.domain.models.valueobject.Relationship;
import com.deposition.domain.port.in.object.RepresentationMetadataParam;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
final class RepresentationMetadataBuilder {

    private final RepresentationMetadataConverter representationMetadataConverter;
    private final RepresentationParamConverter representationParamConverter;
    private final CommonMetadataBuilder commonMetadataBuilder;

    public CommonMetadataBuilder.MetadataStructure buildForRepresentation(List<UUID> fileObjectIds,
            RepresentationMetadataParam baseMetadata,
            Authentication authentication) {
        var objectId = UUID.randomUUID();
        var representation = RepresentationMetadata.builder()
                .id(objectId)
                .relationships(new ArrayList<>(List.of(
                        Relationship.builder()
                                .type(ObjectRelationshipType.STRUCTURAL)
                                .subType(ObjectRelationshipSubType.HAS_PART)
                                .relatedObjects(
                                        fileObjectIds.stream()
                                                .map(fileObjectId -> (RelationObjectIdentifier) RelationObjectIdentifier
                                                .builder()
                                                .type(ObjectIdentifierType.SYSTEM)
                                                .value(fileObjectId
                                                        .toString())
                                                .build())
                                                .toList())
                                .build())))
                .build();
        representationParamConverter.update(representation, baseMetadata);

        var premisRepresentationMetadata = representationMetadataConverter.map(representation);
        return commonMetadataBuilder.toMetadataStructure(objectId, premisRepresentationMetadata, authentication);
    }
}
