package com.deposition.domain.dto.schema.premis.v3.converter;

import com.deposition.domain.dto.schema.premis.v3.*;
import com.deposition.domain.models.RightsStatementMetadata;
import com.deposition.domain.models.valueobject.RightsStatementAgentLink;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

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
    @Mapping(target = "linkingAgentIdentifier", source = "linkingAgentIdentifiers")
    public abstract RightsStatementComplexType map(RightsStatementMetadata metadata);

    /**
     * Wraps a rights statement into PREMIS <rights> container.
     */
    public RightsComplexType wrap(RightsStatementMetadata metadata) {
        var rights = new RightsComplexType();
        rights.setVersion("3.0");
        if (metadata != null) {
            rights.getRightsStatementOrRightsExtension().add(map(metadata));
        }
        return rights;
    }

    protected RightsStatementIdentifierComplexType map(String rightsStatementId) {
        if (rightsStatementId == null || rightsStatementId.isBlank()) {
            return null;
        }
        var id = new RightsStatementIdentifierComplexType();
        id.setRightsStatementIdentifierType(commonConverter.toStringPlusAuthority("SYSTEM"));
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

    @Mapping(target = "linkingAgentRole", source = "roles")
    @Mapping(target = "linkingAgentIdentifierType", source = "agentIdentifier.type")
    @Mapping(target = "linkingAgentIdentifierValue", source = "agentIdentifier.value")
    @Mapping(target = "linkAgentXmlID", ignore = true)
    @Mapping(target = "simpleLink", ignore = true)
    protected abstract LinkingAgentIdentifierComplexType map(RightsStatementAgentLink linkingAgentIdentifiers);
}
