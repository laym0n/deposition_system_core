package com.deposition.domain.dto.schema.premis.v3.converter;

import java.net.URI;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.deposition.domain.dto.schema.premis.v3.ContentLocationComplexType;
import com.deposition.domain.dto.schema.premis.v3.File;
import com.deposition.domain.dto.schema.premis.v3.FixityComplexType;
import com.deposition.domain.dto.schema.premis.v3.FormatDesignationComplexType;
import com.deposition.domain.models.FileMetadata;
import com.deposition.domain.models.valueobject.FixityBlock;
import com.deposition.domain.models.valueobject.FormatDesignation;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR, uses = CommonConverter.class)
public abstract class FileMetadataConverter {

    @Mapping(target = "storage", source = "storages")
    @Mapping(target = "objectCharacteristics", source = "characteristics")
    @Mapping(target = "objectIdentifier", source = "identifiers")
    @Mapping(target = "relationship", source = "relationships")
    @Mapping(target = "objectCharacteristics.compositionLevel", ignore = true)
    @Mapping(target = "objectCharacteristics.creatingApplication", ignore = true)
    @Mapping(target = "objectCharacteristics.inhibitors", ignore = true)
    @Mapping(target = "objectCharacteristics.objectCharacteristicsExtension", ignore = true)
    @Mapping(target = "version", constant = CommonConverter.PREMIS_VERSION)
    @Mapping(target = "xmlID", source = "id", qualifiedByName = "toXmlId")
    public abstract File map(FileMetadata fileMetadata);

    @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "contentLocationType", constant = "URI")
    @Mapping(target = "contentLocationValue", expression = "java(uri.toString())")
    @Mapping(target = "simpleLink", expression = "java(uri.toString())")
    protected abstract ContentLocationComplexType map(URI uri);

    @Mapping(target = "messageDigestAlgorithm", source = "algorithm")
    @Mapping(target = "messageDigest", source = "digest")
    protected abstract FixityComplexType map(FixityBlock fixityBlock);

    @Mapping(target = "formatName", source = "name")
    @Mapping(target = "formatVersion", source = "version")
    protected abstract FormatDesignationComplexType map(FormatDesignation formatDesignation);
}
