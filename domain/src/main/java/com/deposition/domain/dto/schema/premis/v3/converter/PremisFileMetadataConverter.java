package com.deposition.domain.dto.schema.premis.v3.converter;

import java.net.URI;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.deposition.domain.dto.schema.premis.v3.ContentLocationComplexType;
import com.deposition.domain.dto.schema.premis.v3.File;
import com.deposition.domain.dto.schema.premis.v3.FixityComplexType;
import com.deposition.domain.dto.schema.premis.v3.FormatDesignationComplexType;
import com.deposition.domain.dto.schema.premis.v3.StorageComplexType;
import com.deposition.domain.models.FileMetadata;
import com.deposition.domain.models.valueobject.FixityBlock;
import com.deposition.domain.models.valueobject.FormatDesignation;
import com.deposition.domain.models.valueobject.Storage;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = PremisCommonConverter.class)
public abstract class PremisFileMetadataConverter {

    @Autowired
    PremisCommonConverter commonConverter;

    @Mapping(target = "id", expression = "java(commonConverter.extractLocalObjectId(file.getObjectIdentifier()))")
    @Mapping(target = "originalName", source = "originalName.value")
    @Mapping(target = "identifiers", source = "objectIdentifier")
    @Mapping(target = "relationships", source = "relationship")
    @Mapping(target = "characteristics", source = "objectCharacteristics")
    @Mapping(target = "storages", source = "storage")
    public abstract FileMetadata map(File file);

    @Mapping(target = "contentLocation", source = "contentLocation")
    @Mapping(target = "versionId", ignore = true)
    protected abstract Storage map(StorageComplexType storage);

    protected URI map(ContentLocationComplexType contentLocationComplexType) {
        return URI.create(contentLocationComplexType.getContentLocationValue());
    }

    @Mapping(target = "algorithm", source = "messageDigestAlgorithm")
    @Mapping(target = "digest", source = "messageDigest")
    protected abstract FixityBlock map(FixityComplexType fixity);

    @Mapping(target = "name", source = "formatName")
    @Mapping(target = "version", source = "formatVersion")
    protected abstract FormatDesignation map(FormatDesignationComplexType formatDesignation);
}
