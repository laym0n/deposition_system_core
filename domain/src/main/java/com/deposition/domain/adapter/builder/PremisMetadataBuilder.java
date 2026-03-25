package com.deposition.domain.adapter.builder;

import com.deposition.domain.dto.schema.premis.v3.AgentComplexType;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.converter.AgentConverter;
import com.deposition.domain.dto.schema.premis.v3.converter.PremisMetadataConverter;
import com.deposition.domain.models.AgentMetadata;
import com.deposition.domain.models.enums.AgentIdentifierType;
import com.deposition.domain.models.enums.AgentType;
import com.deposition.domain.models.valueobject.AgentIdentifier;
import com.deposition.domain.port.in.object.IntellectualEntityMetadataParam;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public final class PremisMetadataBuilder {

    private final FileMetadataBuilder fileMetadataBuilder;
    private final RepresentationMetadataBuilder representationMetadataBuilder;
    private final IntellectualEntityMetadataBuilder intellectualEntityMetadataBuilder;
    private final PremisMetadataConverter premisConverter;
    private final AgentConverter agentConverter;

    private static AgentMetadata buildCurrentUserAgent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        var agentId = authentication.getName();
        return AgentMetadata.builder()
                .id(agentId)
                .name(agentId)
                .type(AgentType.PERSON)
                .identifiers(List.of(AgentIdentifier.builder()
                        .type(AgentIdentifierType.SYSTEM)
                        .value(agentId)
                        .build()))
                .build();
    }

    public PremisComplexType buildPremisWithEntities(
            List<CommonMetadataBuilder.PersistedRepresentationMetadataInput> persistedRepresentations,
            IntellectualEntityMetadataParam intellectualEntityMetadata, UUID intellectualEntityId) {
        var metadataStructures = new ArrayList<CommonMetadataBuilder.MetadataStructure>();
        var representationObjectIds = new ArrayList<UUID>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        for (var persistedRepresentation : persistedRepresentations) {
            var depositedFileObjectIds = new ArrayList<UUID>();
            for (var persistedFile : persistedRepresentation.persistedFiles()) {
                var fileMetadataStructure = fileMetadataBuilder.buildForFile(persistedFile, authentication);

                depositedFileObjectIds.add(fileMetadataStructure.objectId());
                metadataStructures.add(fileMetadataStructure);
            }

            var representationMetadataStructure = representationMetadataBuilder
                    .buildForRepresentation(depositedFileObjectIds, persistedRepresentation.representationMetadata(),
                            authentication);
            metadataStructures.add(representationMetadataStructure);
            representationObjectIds.add(representationMetadataStructure.objectId());
        }

        var intellectualEntityMetadataStructure = intellectualEntityMetadataBuilder.buildForIntellectualEntity(
                intellectualEntityMetadata, representationObjectIds, intellectualEntityId, authentication);
        metadataStructures.add(intellectualEntityMetadataStructure);

        var events = metadataStructures.stream().map(CommonMetadataBuilder.MetadataStructure::creationEvent)
                .toList();
        var objects = metadataStructures.stream().map(CommonMetadataBuilder.MetadataStructure::objectMetadata)
                .toList();

        var agent = buildCurrentUserAgent(authentication);
        List<AgentComplexType> agents;
        if (agent == null) {
            agents = List.of();
        } else {
            agents = List.of(agentConverter.map(agent));
        }

        return premisConverter.map(objects, events, agents);
    }
}
