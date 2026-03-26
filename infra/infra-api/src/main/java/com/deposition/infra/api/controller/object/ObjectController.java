package com.deposition.infra.api.controller.object;

import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.object.*;
import com.deposition.domain.port.in.schema.IntellectualEntityType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ObjectController {

    private final DeponeInPort deponeInPort;
    private final UpdateMetadataInPort updateMetadataInPort;
    private final GetPremisMetadataInPort getPremisMetadataInPort;
    private final DownloadSourceFilesInPort downloadSourceFilesInPort;
    private final GetCachedObjectMetadataInPort getCachedObjectMetadataInPort;
    private final VerifyPremisInPort verifyPremisInPort;
    private final SearchObjectsInPort searchObjectsInPort;
    private final UpsertDescriptiveMetadataInPort upsertDescriptiveMetadataInPort;

    private static FileMetadataParam resolveFileMetadata(FileMetadataParam fileMetadataParam, MultipartFile file) {
        if (fileMetadataParam == null) {
            return new FileMetadataParam(file.getOriginalFilename());
        }

        // If the client did not provide an originalName, fallback to multipart filename.
        if (fileMetadataParam.originalName() == null) {
            return new FileMetadataParam(file.getOriginalFilename());
        }

        return fileMetadataParam;
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = com.deposition.infra.api.controller.DeponeMultipartRequest.class), encoding = {
            @Encoding(name = "intellectualEntityMetadata", contentType = MediaType.APPLICATION_JSON_VALUE),
            @Encoding(name = "descriptiveMetadata", contentType = MediaType.APPLICATION_JSON_VALUE),
            @Encoding(name = "representationMetadata", contentType = MediaType.APPLICATION_JSON_VALUE),
            @Encoding(name = "fileMetadata", contentType = MediaType.APPLICATION_JSON_VALUE),
            @Encoding(name = "files", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    }))
    @PostMapping(value = "/depone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DepositionResult> depone(
            @RequestParam(name = "intellectualEntityType") IntellectualEntityType intellectualEntityType,
            @RequestPart(name = "intellectualEntityMetadata", required = false) IntellectualEntityMetadataParam intellectualEntityMetadata,
            @RequestPart(name = "descriptiveMetadata", required = false) String descriptiveMetadata,
            @RequestPart(name = "representationMetadata", required = false) RepresentationMetadataParam representationMetadata,
            @RequestPart(name = "fileMetadata", required = false) List<FileMetadataParam> fileMetadata,
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

        if (fileMetadata != null && fileMetadata.size() != files.size()) {
            throw new IllegalArgumentException(
                    "Invalid fileMetadata size: expected " + files.size() + ", got " + fileMetadata.size());
        }

        var deponeFiles = IntStream.range(0, files.size())
                .mapToObj(i -> {
                    var file = files.get(i);
                    var metadataParam = fileMetadata == null ? null : fileMetadata.get(i);
                    var resolvedMetadata = resolveFileMetadata(metadataParam, file);
                    return new DeponeFileParam(resolvedMetadata, convertToReusableResource(file));
                })
                .toList();

        var deponeParams = new DeponeIntellectualEntityParams(
                intellectualEntityType,
                resolvedIntellectualEntityMetadata,
                descriptiveMetadata,
                List.of(new DeponeRepresentationParam(resolvedRepresentationMetadata, deponeFiles)));

        var deponeResult = deponeInPort.depone(deponeParams);
        return ResponseEntity.ok(deponeResult);
    }

    @PatchMapping(value = "/objects/{objectId}/metadata", consumes = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DepositionResult> updateMetadata(
            @PathVariable("objectId") UUID objectId,
            @RequestBody UpdateMetadataParams params) {
        var result = updateMetadataInPort.updateMetadata(objectId, params);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/objects/{objectId}/metadata", produces = MediaType.APPLICATION_XML_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Resource> getPremisMetadata(
            @PathVariable("objectId") UUID objectId,
            @RequestParam(name = "versionId", required = false) String versionId) {
        var premis = getPremisMetadataInPort.getPremisMetadata(objectId, versionId);
        return ResponseEntity.ok(premis);
    }

    @GetMapping(value = "/objects/{objectId}/source-files", produces = "application/zip")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Resource> downloadSourceFiles(
            @PathVariable("objectId") UUID objectId,
            @RequestParam(name = "fileId") List<UUID> fileIds) {
        var zip = downloadSourceFilesInPort.downloadSourceFilesAsZip(objectId, fileIds);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header("Content-Disposition", "attachment; filename=\"" + zip.getFilename() + "\"")
                .body(zip);
    }

    @GetMapping(value = "/objects/{objectId}/cached-metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CachedObjectMetadataResponse> getCachedMetadata(
            @PathVariable("objectId") UUID objectId) {
        var result = getCachedObjectMetadataInPort.getCachedMetadata(objectId);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/objects/{objectId}/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<VerifyPremisResult> verifyPremis(
            @PathVariable("objectId") UUID objectId,
            @RequestParam(name = "versionId", required = false) String versionId) {
        var result = verifyPremisInPort.verifyPremis(objectId, versionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/objects/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<SearchObjectsResult> searchObjects(
            @RequestBody @jakarta.validation.Valid ObjectSearchRequest request) {
        var result = searchObjectsInPort.search(request);
        return ResponseEntity.ok(result);
    }

    @PutMapping(value = "/objects/{objectId}/descriptive-metadata",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> upsertDescriptiveMetadata(
            @PathVariable("objectId") UUID objectId,
            @RequestParam(name = "entityType") IntellectualEntityType entityType,
            @RequestBody String descriptiveMetadataJson) {
        var result = upsertDescriptiveMetadataInPort.upsertDescriptiveMetadata(
                objectId,
                entityType,
                descriptiveMetadataJson);
        return ResponseEntity.ok(result);
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
            throw new IllegalStateException("Failed to read multipart file: " + file.getOriginalFilename(), e);
        }
    }
}
