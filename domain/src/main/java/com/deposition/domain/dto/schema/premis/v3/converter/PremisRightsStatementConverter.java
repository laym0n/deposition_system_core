package com.deposition.domain.dto.schema.premis.v3.converter;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.deposition.domain.dto.schema.premis.v3.RightsStatementComplexType;
import com.deposition.domain.dto.schema.premis.v3.StringPlusAuthority;
import com.deposition.domain.models.RightsStatementMetadata;
import com.deposition.domain.models.valueobject.CopyrightInformation;
import com.deposition.domain.models.valueobject.LicenseInformation;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {
    PremisCommonConverter.class,
    RightsStatementNestedConverter.class
})
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
    @Mapping(target = "identifiers", ignore = true)
    public abstract RightsStatementMetadata map(RightsStatementComplexType complex);

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
}
