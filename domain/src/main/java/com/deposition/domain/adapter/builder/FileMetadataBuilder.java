package com.deposition.domain.adapter.builder;

import com.deposition.domain.adapter.converter.FileParamConverter;
import com.deposition.domain.dto.schema.premis.v3.File;
import com.deposition.domain.dto.schema.premis.v3.converter.FileMetadataConverter;
import com.deposition.domain.models.FileMetadata;
import com.deposition.domain.models.valueobject.Characteristics;
import com.deposition.domain.models.valueobject.FixityBlock;
import com.deposition.domain.models.valueobject.FormatBlock;
import com.deposition.domain.models.valueobject.FormatDesignation;
import com.deposition.domain.port.in.object.DeponeFileParam;
import com.deposition.domain.service.ResourceHashCalculatorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

    private static HashCalculationResult calculateHash(DeponeFileParam fileParam, String hashAlgorithm) {
        try {
            var hash = ResourceHashCalculatorUtils.calculateHash(fileParam.resource(), hashAlgorithm);
            var totalBytes = fileParam.resource().contentLength();
            return new HashCalculationResult(hash, totalBytes);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to calculate hash for deposition file", exception);
        }
    }

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
            CommonMetadataBuilder.PersistedFileMetadataInput persistedFile,
            Authentication authentication) {
        var objectId = UUID.randomUUID();
        var objectMetadata = buildFileObject(persistedFile, objectId);
        return commonMetadataBuilder.toMetadataStructure(objectId, objectMetadata, authentication);
    }

    private File buildFileObject(CommonMetadataBuilder.PersistedFileMetadataInput persistedFile, UUID objectId) {
        var fileParam = persistedFile.fileParam();
        var calculatedHash = calculateHash(fileParam, ResourceHashCalculatorUtils.DEFAULT_HASH_ALGORITHM);

        var fileMetadata = FileMetadata.builder()
                .id(objectId)
                .storages(new ArrayList<>(List.of(persistedFile.fileStorage())))
                .originalName(fileParam.resource().getFilename())
                .characteristics(new ArrayList<>(List.of(
                        Characteristics.builder()
                                .size(calculatedHash.size())
                                .fixity(List.of(
                                        FixityBlock.builder()
                                                .algorithm(ResourceHashCalculatorUtils.DEFAULT_HASH_ALGORITHM)
                                                .digest(calculatedHash.hash())
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

    private static record HashCalculationResult(String hash, long size) {

    }
}
