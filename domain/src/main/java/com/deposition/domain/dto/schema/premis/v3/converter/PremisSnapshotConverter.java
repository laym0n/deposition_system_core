package com.deposition.domain.dto.schema.premis.v3.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.deposition.domain.dto.schema.premis.v3.File;
import com.deposition.domain.dto.schema.premis.v3.IntellectualEntity;
import com.deposition.domain.dto.schema.premis.v3.ObjectComplexType;
import com.deposition.domain.dto.schema.premis.v3.PremisComplexType;
import com.deposition.domain.dto.schema.premis.v3.Representation;
import com.deposition.domain.models.AbstractObjectMetadata;
import com.deposition.domain.models.PremisSnapshot;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {
    FileMetadataConverter.class,
    PremisRepresentationMetadataConverter.class,
    PremisIntellectualEntityMetadataConverter.class,
    PremisEventConverter.class,
    PremisAgentConverter.class,
    PremisRightsStatementConverter.class,
    CommonConverter.class
})
public abstract class PremisSnapshotConverter {

    @Autowired
    protected PremisFileMetadataConverter premisFileMetadataConverter;

    @Autowired
    protected PremisRepresentationMetadataConverter premisRepresentationMetadataConverter;

    @Autowired
    protected PremisRightsStatementConverter premisRightsStatementConverter;

    @Autowired
    protected PremisIntellectualEntityMetadataConverter premisIntellectualEntityMetadataConverter;

    @Mapping(target = "objects", source = "object")
    @Mapping(target = "events", source = "event")
    @Mapping(target = "agents", source = "agent")
    @Mapping(target = "rightsStatements", expression = "java(flattenRightsStatements(premis))")
    public abstract PremisSnapshot map(PremisComplexType premis);

    protected AbstractObjectMetadata map(ObjectComplexType objectComplexType) {
        if (objectComplexType == null) {
            return null;
        }

        return switch (objectComplexType) {
            case File file ->
                premisFileMetadataConverter.map(file);
            case Representation representation ->
                premisRepresentationMetadataConverter.map(representation);
            case IntellectualEntity intellectualEntity ->
                premisIntellectualEntityMetadataConverter.map(intellectualEntity);
            default ->
                null;
        };
    }

    protected java.util.List<com.deposition.domain.models.RightsStatementMetadata> flattenRightsStatements(PremisComplexType premis) {
        if (premis == null || premis.getRights() == null || premis.getRights().isEmpty()) {
            return java.util.List.of();
        }

        var result = new java.util.ArrayList<com.deposition.domain.models.RightsStatementMetadata>();
        for (var rights : premis.getRights()) {
            if (rights == null) {
                continue;
            }
            var mapped = premisRightsStatementConverter.map(rights);
            if (mapped == null || mapped.isEmpty()) {
                continue;
            }
            result.addAll(mapped);
        }
        return java.util.List.copyOf(result);
    }

}
