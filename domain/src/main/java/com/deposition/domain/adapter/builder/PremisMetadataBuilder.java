package com.deposition.domain.adapter.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisMetadataConverter;
import com.deposition.domain.port.in.IntellectualEntityMetadataParam;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class PremisMetadataBuilder {

        private final FileMetadataBuilder fileMetadataBuilder;
        private final RepresentationMetadataBuilder representationMetadataBuilder;
        private final IntellectualEntityMetadataBuilder intellectualEntityMetadataBuilder;
        private final PremisMetadataConverter premisConverter;

        public PremisComplexType buildPremisWithEntities(
                        List<CommonMetadataBuilder.PersistedRepresentationMetadataInput> persistedRepresentations,
                        IntellectualEntityMetadataParam intellectualEntityMetadata, UUID intellectualEntityId) {
                var metadataStructures = new ArrayList<CommonMetadataBuilder.MetadataStructure>();
                var representationObjectIds = new ArrayList<UUID>();

                for (var persistedRepresentation : persistedRepresentations) {
                        var depositedFileObjectIds = new ArrayList<UUID>();
                        for (var persistedFile : persistedRepresentation.persistedFiles()) {
                                var fileMetadataStructure = fileMetadataBuilder.buildForFile(persistedFile);

                                depositedFileObjectIds.add(fileMetadataStructure.objectId());
                                metadataStructures.add(fileMetadataStructure);
                        }

                        var representationMetadataStructure = representationMetadataBuilder
                                        .buildForRepresentation(depositedFileObjectIds, persistedRepresentation.representationMetadata());
                        metadataStructures.add(representationMetadataStructure);
                        representationObjectIds.add(representationMetadataStructure.objectId());
                }

                var intellectualEntityMetadataStructure = intellectualEntityMetadataBuilder.buildForIntellectualEntity(
                                intellectualEntityMetadata, representationObjectIds, intellectualEntityId);
                metadataStructures.add(intellectualEntityMetadataStructure);

                var events = metadataStructures.stream().map(CommonMetadataBuilder.MetadataStructure::creationEvent)
                                .toList();
                var objects = metadataStructures.stream().map(CommonMetadataBuilder.MetadataStructure::objectMetadata)
                                .toList();
                return premisConverter.map(objects, events);
        }
}
