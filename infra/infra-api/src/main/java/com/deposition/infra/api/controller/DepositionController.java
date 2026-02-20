package com.deposition.infra.api.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.deponic.domain.models.ObjectMetadata;
import com.deponic.domain.models.valueobject.Storage;
import com.deponic.domain.port.in.DeponeFileParam;
import com.deponic.domain.port.in.DeponeInPort;
import com.deponic.domain.port.in.DeponeObjectParams;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DepositionController {

    private final DeponeInPort deponeInPort;

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
