package com.deposition.domain.dto.schema.premis.v3.converter;

import com.deposition.domain.dto.schema.premis.v3.LinkingAgentIdentifierComplexType;
import com.deposition.domain.dto.schema.premis.v3.RightsComplexType;
import com.deposition.domain.dto.schema.premis.v3.RightsStatementComplexType;
import com.deposition.domain.dto.schema.premis.v3.StringPlusAuthority;
import com.deposition.domain.models.RightsStatementMetadata;
import com.deposition.domain.models.valueobject.CopyrightInformation;
import com.deposition.domain.models.valueobject.LicenseInformation;
import com.deposition.domain.models.valueobject.RightsStatementAgentLink;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {
        PremisCommonConverter.class,
        RightsStatementNestedConverter.class
}, builder = @Builder(disableBuilder = true))
public abstract class PremisRightsStatementConverter {

    @Autowired
    protected PremisCommonConverter commonConverter;

    @Autowired
    protected RightsStatementNestedConverter nestedConverter;

    @Mapping(target = "id", expression = "java(extractRightsStatementId(complex))")
    @Mapping(target = "rightsBasis", source = "rightsBasis", qualifiedByName = "unwrap")
    @Mapping(target = "copyrightInformation", source = "copyrightInformation", qualifiedByName = "toCopyrightList")
    @Mapping(target = "licenseInformation", source = "licenseInformation", qualifiedByName = "toLicenseList")
    @Mapping(target = "statuteInformation", source = "statuteInformation")
    @Mapping(target = "otherRightsInformation", source = "otherRightsInformation")
    @Mapping(target = "rightsGranted", source = "rightsGranted")
    @Mapping(target = "linkingAgentIdentifiers", source = "linkingAgentIdentifier")
    public abstract RightsStatementMetadata map(RightsStatementComplexType complex);

    /**
     * Maps PREMIS <rights> container to a flat list of rights statements.
     */
    public List<RightsStatementMetadata> map(RightsComplexType rights) {
        if (rights == null || rights.getRightsStatementOrRightsExtension() == null) {
            return List.of();
        }

        var result = new ArrayList<RightsStatementMetadata>();
        for (var item : rights.getRightsStatementOrRightsExtension()) {
            if (!(item instanceof RightsStatementComplexType rs)) {
                continue;
            }
            var mapped = map(rs);
            if (mapped != null) {
                result.add(mapped);
            }
        }
        return List.copyOf(result);
    }

    protected String extractRightsStatementId(RightsStatementComplexType complex) {
        if (complex == null || complex.getRightsStatementIdentifier() == null) {
            return null;
        }
        return complex.getRightsStatementIdentifier().getRightsStatementIdentifierValue();
    }

    @Named("unwrap")
    protected String unwrap(StringPlusAuthority value) {
        return value == null ? null : value.getValue();
    }

    @Named("toCopyrightList")
    protected List<CopyrightInformation> toCopyrightList(
            com.deposition.domain.dto.schema.premis.v3.CopyrightInformationComplexType in) {
        if (in == null) {
            return List.of();
        }
        var mapped = nestedConverter.map(in);
        if (mapped == null) {
            return List.of();
        }
        return List.of(mapped);
    }

    @Named("toLicenseList")
    protected List<LicenseInformation> toLicenseList(
            com.deposition.domain.dto.schema.premis.v3.LicenseInformationComplexType in) {
        if (in == null) {
            return List.of();
        }
        var mapped = nestedConverter.map(in);
        if (mapped == null) {
            return List.of();
        }
        return List.of(mapped);
    }

    protected <T> List<T> map(List<T> in) {
        if (in == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(in);
    }

    @Mapping(target = "roles", source = "linkingAgentRole")
    @Mapping(target = "agentIdentifier.type", source = "linkingAgentIdentifierType.value")
    @Mapping(target = "agentIdentifier.value", source = "linkingAgentIdentifierValue")
    protected abstract RightsStatementAgentLink map(LinkingAgentIdentifierComplexType linkingAgentIdentifier);
}
