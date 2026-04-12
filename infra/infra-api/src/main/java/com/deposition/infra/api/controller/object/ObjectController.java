package com.deposition.infra.api.controller.object;

import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.object.*;
import com.deposition.domain.port.in.schema.IntellectualEntityType;
import com.deposition.infra.api.controller.DeponeMultipartForm;
import com.deposition.infra.api.controller.DeponeMultipartRequest;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = DeponeMultipartForm.class), encoding = {
            @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE),
            @Encoding(name = "files", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    }))
    @PostMapping(value = "/depone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DepositionResult> depone(
            @RequestParam(name = "intellectualEntityType") IntellectualEntityType intellectualEntityType,
            @RequestPart(name = "request", required = false) DeponeMultipartRequest request,
            @RequestPart(name = "files") List<MultipartFile> files) {

        var intellectualEntityMetadata = request == null ? null : request.intellectualEntityMetadata;
        var descriptiveMetadata = request == null ? null : request.descriptiveMetadata;
        var representationMetadata = request == null ? null : request.representationMetadata;
        var fileMetadata = request == null ? null : request.fileMetadata;

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

        try {
            var deponeResult = deponeInPort.depone(deponeParams);
            return ResponseEntity.ok(deponeResult);
        } finally {
            cleanupTempResources(deponeFiles);
        }
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
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            Files.createDirectories(tmpDir.toPath());

            String suffix = sanitizeSuffix(file.getOriginalFilename());
            File tmp = File.createTempFile("deposition-upload-", suffix, tmpDir);
            file.transferTo(tmp);

            return new TempFileResource(tmp, file.getOriginalFilename());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to persist multipart file to temp storage: " + file.getOriginalFilename(), e);
        }
    }

    private static void cleanupTempResources(List<DeponeFileParam> deponeFiles) {
        if (deponeFiles == null || deponeFiles.isEmpty()) {
            return;
        }

        for (var fileParam : deponeFiles) {
            if (fileParam == null || fileParam.resource() == null) {
                continue;
            }

            var res = fileParam.resource();
            if (res instanceof TempFileResource temp) {
                // best-effort cleanup
                try {
                    Files.deleteIfExists(temp.getTempFile().toPath());
                } catch (Exception ignored) {
                    // ignore cleanup failures: the temp folder will be cleaned by OS eventually
                }
            }
        }
    }

    private static final class TempFileResource extends FileSystemResource {
        private final File tempFile;
        private final String originalFilename;

        private TempFileResource(File tempFile, String originalFilename) {
            super(tempFile);
            this.tempFile = tempFile;
            this.originalFilename = originalFilename;
        }

        @Override
        public String getFilename() {
            return originalFilename;
        }

        @Override
        public long contentLength() {
            return tempFile.length();
        }

        private File getTempFile() {
            return tempFile;
        }
    }

    private static String sanitizeSuffix(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return ".bin";
        }

        String safe = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        int dot = safe.lastIndexOf('.');
        if (dot < 0 || dot == safe.length() - 1) {
            return ".bin";
        }
        return safe.substring(dot);
    }
}
