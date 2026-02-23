package com.deposition.infra.api.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.deposition.domain.port.in.DeponeFileParam;
import com.deposition.domain.port.in.DeponeInPort;
import com.deposition.domain.port.in.DeponeIntellectualEntityParams;
import com.deposition.domain.port.in.DeponeRepresentationParam;
import com.deposition.domain.port.in.DeponeResult;
import com.deposition.domain.port.in.FileMetadataParam;
import com.deposition.domain.port.in.IntellectualEntityMetadataParam;
import com.deposition.domain.port.in.RepresentationMetadataParam;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DepositionController {

        private final DeponeInPort deponeInPort;

        @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = DeponeMultipartRequest.class), encoding = {
                        @Encoding(name = "intellectualEntityMetadata", contentType = MediaType.APPLICATION_JSON_VALUE),
                        @Encoding(name = "representationMetadata", contentType = MediaType.APPLICATION_JSON_VALUE),
                        @Encoding(name = "files", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        }))
        @PostMapping(value = "/depone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<DeponeResult> depone(
                        @RequestPart(name = "intellectualEntityMetadata", required = false) IntellectualEntityMetadataParam intellectualEntityMetadata,
                        @RequestPart(name = "representationMetadata", required = false) RepresentationMetadataParam representationMetadata,
                        @RequestPart(name = "files") List<MultipartFile> files) {

                var resolvedIntellectualEntityMetadata = intellectualEntityMetadata == null
                                ? new IntellectualEntityMetadataParam(null, List.of(), List.of())
                                : new IntellectualEntityMetadataParam(
                                                intellectualEntityMetadata.originalName(),
                                                intellectualEntityMetadata.identifiers() == null
                                                                ? List.of()
                                                                : List.copyOf(intellectualEntityMetadata.identifiers()),
                                                intellectualEntityMetadata.relationships() == null
                                                                ? List.of()
                                                                : List.copyOf(intellectualEntityMetadata
                                                                                .relationships()));

                var resolvedRepresentationMetadata = representationMetadata == null
                                ? new RepresentationMetadataParam(null)
                                : representationMetadata;

                var deponeFiles = files.stream()
                                .map(file -> new DeponeFileParam(
                                                new FileMetadataParam(file.getOriginalFilename()),
                                                convertToReusableResource(file)))
                                .toList();

                var deponeParams = new DeponeIntellectualEntityParams(
                                resolvedIntellectualEntityMetadata,
                                List.of(new DeponeRepresentationParam(resolvedRepresentationMetadata, deponeFiles)));

                var deponeResult = deponeInPort.depone(deponeParams);
                return ResponseEntity.ok(deponeResult);
        }

        private Resource convertToReusableResource(MultipartFile file) {
                try {
                        byte[] bytes = file.getBytes();
                        return new ByteArrayResource(bytes) {
                                @Override
                                public String getFilename() {
                                        return file.getOriginalFilename();
                                }
                        };
                } catch (IOException e) {
                        throw new IllegalStateException("Failed to read multipart file: " + file.getOriginalFilename(),
                                        e);
                }
        }
}
