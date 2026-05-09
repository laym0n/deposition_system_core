package com.deposition.infra.api.controller.object;

import com.deposition.domain.port.in.common.DepositionResult;
import com.deposition.domain.port.in.object.*;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
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
    private final PresignSourceFilesDownloadInPort presignSourceFilesDownloadInPort;
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
            @RequestParam(name = "intellectualEntityType") String intellectualEntityTypeName,
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
                    return new DeponeFileParam(resolvedMetadata, toReusableResource(file));
                })
                .toList();

        var deponeParams = new DeponeIntellectualEntityParams(
                intellectualEntityTypeName,
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

    @GetMapping(value = "/objects/{objectId}/source-files/presigned", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<PresignedSourceFilesDownloadResponse>> presignSourceFilesDownload(
            @PathVariable("objectId") UUID objectId,
            @RequestParam(name = "fileId") List<UUID> fileIds) {
        var result = presignSourceFilesDownloadInPort.presignSourceFilesDownload(objectId, fileIds);
        var response = result.stream()
                .map(r -> new PresignedSourceFilesDownloadResponse(
                        r.fileId(),
                        r.downloadUrl(),
                        r.expiresAt()))
                .toList();
        return ResponseEntity.ok(response);
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
            @RequestParam(name = "entityType") String entityTypeName,
            @RequestBody String descriptiveMetadataJson) {
        var result = upsertDescriptiveMetadataInPort.upsertDescriptiveMetadata(
                objectId,
                entityTypeName,
                descriptiveMetadataJson);
        return ResponseEntity.ok(result);
    }

    private static Resource toReusableResource(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }

        var delegate = file.getResource();
        var originalFilename = file.getOriginalFilename();

        return new Resource() {
            @Override
            public boolean exists() {
                return delegate.exists();
            }

            @Override
            public boolean isReadable() {
                return delegate.isReadable();
            }

            @Override
            public boolean isOpen() {
                return delegate.isOpen();
            }

            @Override
            public URL getURL() throws IOException {
                return delegate.getURL();
            }

            @Override
            public URI getURI() throws IOException {
                return delegate.getURI();
            }

            @Override
            public File getFile() throws IOException {
                return delegate.getFile();
            }

            @Override
            public long contentLength() throws IOException {
                return delegate.contentLength();
            }

            @Override
            public long lastModified() throws IOException {
                return delegate.lastModified();
            }

            @Override
            public Resource createRelative(String relativePath) throws IOException {
                return delegate.createRelative(relativePath);
            }

            @Override
            public String getFilename() {
                return originalFilename != null ? originalFilename : delegate.getFilename();
            }

            @Override
            public String getDescription() {
                return "MultipartFileResource(" + getFilename() + ")";
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return delegate.getInputStream();
            }
        };
    }
}
