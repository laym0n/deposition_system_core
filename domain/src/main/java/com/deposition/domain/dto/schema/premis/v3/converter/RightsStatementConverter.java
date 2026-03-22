package com.deposition.domain.dto.schema.premis.v3.converter;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.deposition.domain.dto.schema.premis.v3.RightsStatementComplexType;
import com.deposition.domain.dto.schema.premis.v3.RightsStatementIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.StringPlusAuthority;
import com.deposition.domain.models.RightsStatementMetadata;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, uses = {
    CommonConverter.class,
    RightsStatementNestedConverter.class
})
public abstract class RightsStatementConverter {

    @Autowired
    protected CommonConverter commonConverter;

    @Mapping(target = "rightsStatementIdentifier", source = "id")
    @Mapping(target = "rightsBasis", source = "rightsBasis", qualifiedByName = "mapRightsBasis")
    @Mapping(target = "copyrightInformation", source = "copyrightInformation", qualifiedByName = "mapCopyrightInformation")
    @Mapping(target = "licenseInformation", source = "licenseInformation", qualifiedByName = "mapLicenseInformation")
    @Mapping(target = "statuteInformation", source = "statuteInformation")
    @Mapping(target = "otherRightsInformation", source = "otherRightsInformation")
    @Mapping(target = "rightsGranted", source = "rightsGranted")
    @Mapping(target = "linkingObjectIdentifier", ignore = true)
    @Mapping(target = "linkingAgentIdentifier", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = "identifiers")
    public abstract RightsStatementComplexType map(RightsStatementMetadata metadata);

    protected RightsStatementIdentifierComplexType map(String rightsStatementId) {
        if (rightsStatementId == null || rightsStatementId.isBlank()) {
            return null;
        }
        var id = new RightsStatementIdentifierComplexType();
        id.setRightsStatementIdentifierType(commonConverter.toStringPlusAuthority("LOCAL"));
        id.setRightsStatementIdentifierValue(rightsStatementId);
        return id;
    }

    @Named("mapRightsBasis")
    protected StringPlusAuthority mapRightsBasis(String rightsBasis) {
        if (rightsBasis == null || rightsBasis.isBlank()) {
            return null;
        }
        return commonConverter.toStringPlusAuthority(rightsBasis);
    }
}
