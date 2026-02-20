package com.deposition.infra.api.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.deposition.domain.models.ObjectMetadata;
import com.deposition.domain.models.valueobject.Storage;
import com.deposition.domain.port.in.DeponeFileParam;
import com.deposition.domain.port.in.DeponeInPort;
import com.deposition.domain.port.in.DeponeObjectParams;

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
                        @Encoding(name = "storages", contentType = MediaType.APPLICATION_JSON_VALUE),
                        @Encoding(name = "files", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        }))
        @PostMapping(value = "/depone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<Void> depone(
                        @RequestPart(name = "intellectualEntityMetadata", required = false) ObjectMetadata intellectualEntityMetadata,
                        @RequestPart(name = "storages", required = false) List<Storage> storages,
                        @RequestPart(name = "files") List<MultipartFile> files) {

                var resolvedStorages = storages == null ? List.<Storage>of() : List.copyOf(storages);

                var deponeFiles = files.stream()
                                .map(file -> new DeponeFileParam(file.getResource(), resolvedStorages))
                                .toList();

                deponeInPort.depone(new DeponeObjectParams(intellectualEntityMetadata, deponeFiles));
                return ResponseEntity.ok().build();
        }
}
