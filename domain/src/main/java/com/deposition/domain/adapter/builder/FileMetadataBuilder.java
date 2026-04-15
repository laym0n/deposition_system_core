package com.deposition.domain.adapter.builder;

import com.deposition.domain.adapter.converter.FileParamConverter;
import com.deposition.domain.dto.schema.premis.v3.File;
import com.deposition.domain.dto.schema.premis.v3.converter.FileMetadataConverter;
import com.deposition.domain.models.FileMetadata;
import com.deposition.domain.models.valueobject.Characteristics;
import com.deposition.domain.models.valueobject.FixityBlock;
import com.deposition.domain.models.valueobject.FormatBlock;
import com.deposition.domain.models.valueobject.FormatDesignation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
final class FileMetadataBuilder {

    private static final String DEFAULT_FORMAT_NAME = "application/octet-stream";

    private final FileMetadataConverter fileMetadataConverter;
    private final FileParamConverter fileParamConverter;
    private final CommonMetadataBuilder commonMetadataBuilder;

    private static String toFormatName(Resource resource) {
        var filename = resource.getFilename();
        if (filename == null || filename.isBlank()) {
            return DEFAULT_FORMAT_NAME;
        }

        var contentType = URLConnection.guessContentTypeFromName(filename);
        if (contentType == null || contentType.isBlank()) {
            return DEFAULT_FORMAT_NAME;
        }
        return contentType;
    }

    public CommonMetadataBuilder.MetadataStructure buildForFile(
            CommonMetadataBuilder.PersistedFileMetadataInput persistedFile) {
        var objectId = UUID.randomUUID();
        var objectMetadata = buildFileObject(persistedFile, objectId);
        return commonMetadataBuilder.toMetadataStructure(objectId, objectMetadata);
    }

    private File buildFileObject(CommonMetadataBuilder.PersistedFileMetadataInput persistedFile, UUID objectId) {
        var fileParam = persistedFile.fileParam();
        var hashAlgorithm = persistedFile.hashAlgorithm();
        var hashHex = persistedFile.hashHex();
        var sizeBytes = persistedFile.sizeBytes();

        var fileMetadata = FileMetadata.builder()
                .id(objectId)
                .storages(new ArrayList<>(List.of(persistedFile.fileStorage())))
                .originalName(fileParam.resource().getFilename())
                .characteristics(new ArrayList<>(List.of(
                        Characteristics.builder()
                                .size(sizeBytes)
                                .fixity(List.of(
                                        FixityBlock.builder()
                                                .algorithm(hashAlgorithm)
                                                .digest(hashHex)
                                                .build()))
                                .format(List.of(
                                        FormatBlock.builder()
                                                .formatDesignation(FormatDesignation.builder()
                                                        .name(toFormatName(fileParam.resource()))
                                                        .build())
                                                .build()))
                                .build())))
                .build();

        fileParamConverter.update(fileMetadata, fileParam.fileMetadata()); // TODO сделать правильное обновление storage (добавление к создаваемому в системе)
        return fileMetadataConverter.map(fileMetadata);
    }
}
