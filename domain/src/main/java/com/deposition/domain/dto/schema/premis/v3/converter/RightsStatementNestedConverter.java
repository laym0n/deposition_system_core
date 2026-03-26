package com.deposition.domain.dto.schema.premis.v3.converter;

import com.deposition.domain.dto.schema.premis.v3.*;
import com.deposition.domain.models.valueobject.*;
import jakarta.xml.bind.JAXBElement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper converter for nested structures of PREMIS rightsStatement.
 * <p>
 * Separated to keep {@link RightsStatementConverter} readable and to reuse in
 * PREMIS -> model converter.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = CommonConverter.class)
public abstract class RightsStatementNestedConverter {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    @Autowired
    protected CommonConverter commonConverter;
    @Autowired
    protected PremisCommonConverter premisCommonConverter;

    /* =========================
     * Model -> PREMIS (JAXB)
     * ========================= */
    @Mapping(target = "copyrightStatus", source = "copyrightStatus")
    @Mapping(target = "copyrightJurisdiction", source = "copyrightJurisdiction", qualifiedByName = "toCountryCode")
    @Mapping(target = "copyrightStatusDeterminationDate", source = "copyrightStatusDeterminationDate", qualifiedByName = "localDateToString")
    @Mapping(target = "copyrightNote", source = "copyrightNote")
    @Mapping(target = "copyrightDocumentationIdentifier", source = "documentationIdentifiers")
    @Mapping(target = "copyrightApplicableDates", source = "applicableDates", qualifiedByName = "toStartAndEndDates")
    public abstract CopyrightInformationComplexType map(CopyrightInformation in);

    public CopyrightInformationComplexType mapCopyrightInformationList(List<CopyrightInformation> in) {
        if (in == null || in.isEmpty()) {
            return null;
        }
        // PREMIS schema allows only one copyrightInformation.
        return map(in.getFirst());
    }

    protected CopyrightInformationComplexType mapCopyrightInformation(List<CopyrightInformation> in) {
        return mapCopyrightInformationList(in);
    }

    @Named("mapCopyrightInformation")
    protected CopyrightInformationComplexType mapCopyrightInformationNamed(List<CopyrightInformation> in) {
        return mapCopyrightInformation(in);
    }

    @Mapping(target = "copyrightDocumentationIdentifierType", source = "type")
    @Mapping(target = "copyrightDocumentationIdentifierValue", source = "value")
    @Mapping(target = "copyrightDocumentationRole", source = "role")
    protected abstract CopyrightDocumentationIdentifierComplexType mapCopyrightDoc(DocumentationIdentifier in);

    public LicenseInformationComplexType mapLicenseInformationList(List<LicenseInformation> in) {
        if (in == null || in.isEmpty()) {
            return null;
        }
        // PREMIS schema allows only one licenseInformation.
        return map(in.getFirst());
    }

    protected LicenseInformationComplexType mapLicenseInformation(List<LicenseInformation> in) {
        return mapLicenseInformationList(in);
    }

    @Named("mapLicenseInformation")
    protected LicenseInformationComplexType mapLicenseInformationNamed(List<LicenseInformation> in) {
        return mapLicenseInformation(in);
    }

    public LicenseInformationComplexType map(LicenseInformation in) {
        if (in == null) {
            return null;
        }
        var out = new LicenseInformationComplexType();
        var content = out.getContent();

        if (in.getDocumentationIdentifiers() != null) {
            for (var d : in.getDocumentationIdentifiers()) {
                if (d == null) {
                    continue;
                }
                var doc = mapLicenseDoc(d);
                content.add(OBJECT_FACTORY.createLicenseDocumentationIdentifier(doc));
            }
        }
        if (in.getLicenseTerms() != null) {
            content.add(OBJECT_FACTORY.createLicenseTerms(in.getLicenseTerms()));
        }
        if (in.getLicenseNote() != null) {
            for (var n : in.getLicenseNote()) {
                if (n == null) {
                    continue;
                }
                content.add(OBJECT_FACTORY.createLicenseNote(n));
            }
        }
        if (in.getApplicableDates() != null) {
            var dates = toStartAndEndDates(in.getApplicableDates());
            content.add(OBJECT_FACTORY.createLicenseApplicableDates(dates));
        }

        return out;
    }

    @Mapping(target = "licenseDocumentationIdentifierType", source = "type")
    @Mapping(target = "licenseDocumentationIdentifierValue", source = "value")
    @Mapping(target = "licenseDocumentationRole", source = "role")
    protected abstract LicenseDocumentationIdentifierComplexType mapLicenseDoc(DocumentationIdentifier in);

    @Mapping(target = "otherRightsDocumentationIdentifier", source = "documentationIdentifiers")
    @Mapping(target = "otherRightsBasis", source = "otherRightsBasis")
    @Mapping(target = "otherRightsApplicableDates", source = "applicableDates", qualifiedByName = "toStartAndEndDates")
    @Mapping(target = "otherRightsNote", source = "otherRightsNote")
    public abstract OtherRightsInformationComplexType map(OtherRightsInformation in);

    @Mapping(target = "otherRightsDocumentationIdentifierType", source = "type")
    @Mapping(target = "otherRightsDocumentationIdentifierValue", source = "value")
    @Mapping(target = "otherRightsDocumentationRole", source = "role")
    protected abstract OtherRightsDocumentationIdentifierComplexType mapOtherRightsDoc(DocumentationIdentifier in);

    @Mapping(target = "statuteJurisdiction", source = "statuteJurisdiction", qualifiedByName = "toCountryCode")
    @Mapping(target = "statuteCitation", source = "statuteCitation")
    @Mapping(target = "statuteInformationDeterminationDate", source = "statuteInformationDeterminationDate", qualifiedByName = "localDateToString")
    @Mapping(target = "statuteNote", source = "statuteNote")
    @Mapping(target = "statuteDocumentationIdentifier", source = "documentationIdentifiers")
    @Mapping(target = "statuteApplicableDates", source = "applicableDates", qualifiedByName = "toStartAndEndDates")
    public abstract StatuteInformationComplexType map(StatuteInformation in);

    @Mapping(target = "statuteDocumentationIdentifierType", source = "type")
    @Mapping(target = "statuteDocumentationIdentifierValue", source = "value")
    @Mapping(target = "statuteDocumentationRole", source = "role")
    protected abstract StatuteDocumentationIdentifierComplexType mapStatuteDoc(DocumentationIdentifier in);

    @Mapping(target = "act", source = "act")
    @Mapping(target = "restriction", source = "restriction", qualifiedByName = "toStringPlusAuthorityList")
    @Mapping(target = "termOfGrant", source = "termOfGrant", qualifiedByName = "toStartAndEndDates")
    @Mapping(target = "termOfRestriction", source = "termOfRestriction", qualifiedByName = "toStartAndEndDates")
    @Mapping(target = "rightsGrantedNote", source = "rightsGrantedNote")
    public abstract RightsGrantedComplexType map(RightsGranted in);

    /* =========================
     * PREMIS (JAXB) -> Model
     * ========================= */
    @Mapping(target = "type", expression = "java(premisCommonConverter.unwrapStringPlusAuthority(in.getCopyrightDocumentationIdentifierType()))")
    @Mapping(target = "value", source = "copyrightDocumentationIdentifierValue")
    @Mapping(target = "role", expression = "java(premisCommonConverter.unwrapStringPlusAuthority(in.getCopyrightDocumentationRole()))")
    protected abstract DocumentationIdentifier mapCopyrightDoc(CopyrightDocumentationIdentifierComplexType in);

    @Mapping(target = "type", expression = "java(premisCommonConverter.unwrapStringPlusAuthority(in.getLicenseDocumentationIdentifierType()))")
    @Mapping(target = "value", source = "licenseDocumentationIdentifierValue")
    @Mapping(target = "role", expression = "java(premisCommonConverter.unwrapStringPlusAuthority(in.getLicenseDocumentationRole()))")
    protected abstract DocumentationIdentifier mapLicenseDoc(LicenseDocumentationIdentifierComplexType in);

    @Mapping(target = "type", expression = "java(premisCommonConverter.unwrapStringPlusAuthority(in.getOtherRightsDocumentationIdentifierType()))")
    @Mapping(target = "value", source = "otherRightsDocumentationIdentifierValue")
    @Mapping(target = "role", expression = "java(premisCommonConverter.unwrapStringPlusAuthority(in.getOtherRightsDocumentationRole()))")
    protected abstract DocumentationIdentifier mapOtherRightsDoc(OtherRightsDocumentationIdentifierComplexType in);

    @Mapping(target = "type", expression = "java(premisCommonConverter.unwrapStringPlusAuthority(in.getStatuteDocumentationIdentifierType()))")
    @Mapping(target = "value", source = "statuteDocumentationIdentifierValue")
    @Mapping(target = "role", expression = "java(premisCommonConverter.unwrapStringPlusAuthority(in.getStatuteDocumentationRole()))")
    protected abstract DocumentationIdentifier mapStatuteDoc(StatuteDocumentationIdentifierComplexType in);

    public CopyrightInformation map(CopyrightInformationComplexType in) {
        if (in == null) {
            return null;
        }
        return CopyrightInformation.builder()
                .copyrightStatus(premisCommonConverter.unwrapStringPlusAuthority(in.getCopyrightStatus()))
                .copyrightJurisdiction(premisCommonConverter.unwrapStringPlusAuthority(in.getCopyrightJurisdiction()))
                .copyrightStatusDeterminationDate(parseLocalDate(in.getCopyrightStatusDeterminationDate()))
                .copyrightNote(in.getCopyrightNote() == null ? null : List.copyOf(in.getCopyrightNote()))
                .documentationIdentifiers(mapDocumentationIdentifiers(in.getCopyrightDocumentationIdentifier()))
                .applicableDates(mapApplicableDates(in.getCopyrightApplicableDates()))
                .build();
    }

    public LicenseInformation map(LicenseInformationComplexType in) {
        if (in == null) {
            return null;
        }

        List<DocumentationIdentifier> docs = new ArrayList<>();
        String terms = null;
        List<String> notes = new ArrayList<>();
        ApplicableDates dates = null;

        if (in.getContent() != null) {
            for (JAXBElement<?> element : in.getContent()) {
                if (element == null) {
                    continue;
                }
                var value = element.getValue();
                if (value instanceof LicenseDocumentationIdentifierComplexType doc) {
                    docs.add(mapLicenseDoc(doc));
                } else if (value instanceof String s) {
                    // Could be licenseTerms or licenseNote, differentiated by element name.
                    String localName = element.getName() == null ? null : element.getName().getLocalPart();
                    if ("licenseTerms".equals(localName)) {
                        terms = s;
                    } else if ("licenseNote".equals(localName)) {
                        notes.add(s);
                    }
                } else if (value instanceof StartAndEndDateComplexType sed) {
                    dates = mapApplicableDates(sed);
                }
            }
        }

        return LicenseInformation.builder()
                .documentationIdentifiers(docs.isEmpty() ? null : Collections.unmodifiableList(docs))
                .licenseTerms(terms)
                .licenseNote(notes.isEmpty() ? null : Collections.unmodifiableList(notes))
                .applicableDates(dates)
                .build();
    }

    public OtherRightsInformation map(OtherRightsInformationComplexType in) {
        if (in == null) {
            return null;
        }
        return OtherRightsInformation.builder()
                .documentationIdentifiers(mapDocumentationIdentifiers(in.getOtherRightsDocumentationIdentifier()))
                .otherRightsBasis(premisCommonConverter.unwrapStringPlusAuthority(in.getOtherRightsBasis()))
                .applicableDates(mapApplicableDates(in.getOtherRightsApplicableDates()))
                .otherRightsNote(in.getOtherRightsNote() == null ? null : List.copyOf(in.getOtherRightsNote()))
                .build();
    }

    public StatuteInformation map(StatuteInformationComplexType in) {
        if (in == null) {
            return null;
        }
        return StatuteInformation.builder()
                .statuteJurisdiction(premisCommonConverter.unwrapStringPlusAuthority(in.getStatuteJurisdiction()))
                .statuteCitation(premisCommonConverter.unwrapStringPlusAuthority(in.getStatuteCitation()))
                .statuteInformationDeterminationDate(parseLocalDate(in.getStatuteInformationDeterminationDate()))
                .statuteNote(in.getStatuteNote() == null ? null : List.copyOf(in.getStatuteNote()))
                .documentationIdentifiers(mapDocumentationIdentifiers(in.getStatuteDocumentationIdentifier()))
                .applicableDates(mapApplicableDates(in.getStatuteApplicableDates()))
                .build();
    }

    public RightsGranted map(RightsGrantedComplexType in) {
        if (in == null) {
            return null;
        }
        return RightsGranted.builder()
                .act(premisCommonConverter.unwrapStringPlusAuthority(in.getAct()))
                .restriction(mapStringPlusAuthorityList(in.getRestriction()))
                .termOfGrant(mapApplicableDates(in.getTermOfGrant()))
                .termOfRestriction(mapApplicableDates(in.getTermOfRestriction()))
                .rightsGrantedNote(in.getRightsGrantedNote() == null ? null : List.copyOf(in.getRightsGrantedNote()))
                .build();
    }

    /* =========================
     * Common helpers
     * ========================= */
    @Named("toCountryCode")
    protected CountryCode toCountryCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        var out = new CountryCode();
        out.setValue(code);
        return out;
    }

    @Named("localDateToString")
    protected String localDateToString(LocalDate date) {
        return date == null ? null : date.toString();
    }

    protected LocalDate parseLocalDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalDate.parse(raw);
    }

    @Named("toStartAndEndDates")
    protected StartAndEndDateComplexType toStartAndEndDates(ApplicableDates dates) {
        if (dates == null || (dates.getStartDate() == null && dates.getEndDate() == null)) {
            return null;
        }
        var out = new StartAndEndDateComplexType();
        // PREMIS schema has required startDate but their code earlier allowed empty string.
        out.setStartDate(dates.getStartDate() == null ? "" : dates.getStartDate().toString());
        if (dates.getEndDate() != null) {
            out.setEndDate(dates.getEndDate().toString());
        }
        return out;
    }

    protected ApplicableDates mapApplicableDates(StartAndEndDateComplexType in) {
        if (in == null) {
            return null;
        }
        ZonedDateTime start = null;
        ZonedDateTime end = null;
        try {
            if (in.getStartDate() != null && !in.getStartDate().isBlank()) {
                start = ZonedDateTime.parse(in.getStartDate());
            }
        } catch (RuntimeException ex) {
            // ignore parse errors
        }
        try {
            if (in.getEndDate() != null && !in.getEndDate().isBlank()) {
                end = ZonedDateTime.parse(in.getEndDate());
            }
        } catch (RuntimeException ex) {
            // ignore parse errors
        }
        if (start == null && end == null) {
            return null;
        }
        return new ApplicableDates(start, end);
    }

    protected List<DocumentationIdentifier> mapDocumentationIdentifiers(
            List<? extends Object> in) {
        if (in == null || in.isEmpty()) {
            return null;
        }
        var result = new ArrayList<DocumentationIdentifier>(in.size());
        for (var item : in) {
            if (item instanceof CopyrightDocumentationIdentifierComplexType c) {
                result.add(mapCopyrightDoc(c));
            } else if (item instanceof LicenseDocumentationIdentifierComplexType l) {
                result.add(mapLicenseDoc(l));
            } else if (item instanceof StatuteDocumentationIdentifierComplexType s) {
                result.add(mapStatuteDoc(s));
            } else if (item instanceof OtherRightsDocumentationIdentifierComplexType o) {
                result.add(mapOtherRightsDoc(o));
            }
        }
        return result.isEmpty() ? null : Collections.unmodifiableList(result);
    }

    @Named("toStringPlusAuthorityList")
    protected List<StringPlusAuthority> toStringPlusAuthorityList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        var result = new ArrayList<StringPlusAuthority>(values.size());
        for (var v : values) {
            if (v == null) {
                continue;
            }
            result.add(commonConverter.toStringPlusAuthority(v));
        }
        return Collections.unmodifiableList(result);
    }

    protected List<String> mapStringPlusAuthorityList(List<StringPlusAuthority> in) {
        if (in == null || in.isEmpty()) {
            return null;
        }
        var result = new ArrayList<String>(in.size());
        for (var v : in) {
            var raw = premisCommonConverter.unwrapStringPlusAuthority(v);
            if (raw == null || raw.isBlank()) {
                continue;
            }
            result.add(raw);
        }
        return result.isEmpty() ? null : Collections.unmodifiableList(result);
    }
}
