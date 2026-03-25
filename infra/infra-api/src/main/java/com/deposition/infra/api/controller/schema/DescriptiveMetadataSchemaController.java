package com.deposition.infra.api.controller.schema;

import com.deposition.domain.models.DescriptiveMetadataSchema;
import com.deposition.domain.port.in.schema.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class DescriptiveMetadataSchemaController {

    private final GetDescriptiveMetadataSchemaByIdInPort getDescriptiveMetadataSchemaByIdInPort;
    private final GetDescriptiveMetadataSchemasInPort getDescriptiveMetadataSchemasInPort;
    private final CreateDescriptiveMetadataSchemaInPort createDescriptiveMetadataSchemaInPort;
    private final UpdateDescriptiveMetadataSchemaActiveInPort updateDescriptiveMetadataSchemaActiveInPort;

    @GetMapping(value = "/descriptive-metadata/schemas", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<DescriptiveMetadataSchemaSummaryDto>> getSchemas(
            @RequestParam(name = "entityType", required = false) IntellectualEntityType entityType,
            @RequestParam(name = "active", required = false) Boolean active) {
        var effectiveEntityType = entityType;
        var filter = new GetDescriptiveMetadataSchemasInPort.DescriptiveMetadataSchemaFilter(effectiveEntityType, active);
        var schemas = getDescriptiveMetadataSchemasInPort.getSchemas(filter);
        return ResponseEntity.ok(schemas.stream().map(DescriptiveMetadataSchemaSummaryDto::from).toList());
    }

    @GetMapping(value = "/descriptive-metadata/schemas/{schemaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, Object>> getSchemaById(
            @PathVariable("schemaId") UUID schemaId) {
        var schema = getDescriptiveMetadataSchemaByIdInPort.getSchema(schemaId);
        return ResponseEntity.ok(schema);
    }

    @PostMapping(value = "/descriptive-metadata/schema", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DescriptiveMetadataSchemaDto> createSchema(@RequestBody CreateSchemaRequest request) {
        var created = createDescriptiveMetadataSchemaInPort.create(
                new CreateDescriptiveMetadataSchemaInPort.CreateDescriptiveMetadataSchemaCommand(
                        request.entityType(),
                        request.schemaJson()));
        return ResponseEntity.status(HttpStatus.CREATED).body(DescriptiveMetadataSchemaDto.from(created));
    }

    @PutMapping(value = "/descriptive-metadata/schema/{schemaId}/active", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DescriptiveMetadataSchemaDto> updateActive(
            @PathVariable("schemaId") UUID schemaId,
            @RequestBody UpdateActiveRequest request) {
        var updated = updateDescriptiveMetadataSchemaActiveInPort.updateActive(
                schemaId,
                new UpdateDescriptiveMetadataSchemaActiveInPort.UpdateActiveCommand(request.active()));
        return ResponseEntity.ok(DescriptiveMetadataSchemaDto.from(updated));
    }

    public record DescriptiveMetadataSchemaSummaryDto(
            UUID id,
            IntellectualEntityType entityType,
            boolean active,
            java.time.OffsetDateTime createdAt,
            java.time.OffsetDateTime updatedAt) {

        public static DescriptiveMetadataSchemaSummaryDto from(
                GetDescriptiveMetadataSchemasInPort.DescriptiveMetadataSchemaSummary s) {
            return new DescriptiveMetadataSchemaSummaryDto(
                    s.id(),
                    s.entityType(),
                    s.active(),
                    s.createdAt(),
                    s.updatedAt());
        }
    }

    public record CreateSchemaRequest(
            @NotNull IntellectualEntityType entityType,
            @NotNull String schemaJson) {

    }

    public record UpdateActiveRequest(@NotNull Boolean active) {

    }

    public record DescriptiveMetadataSchemaDto(
            UUID id,
            IntellectualEntityType entityType,
            String schemaJson,
            boolean active,
            java.time.OffsetDateTime createdAt,
            java.time.OffsetDateTime updatedAt) {

        public static DescriptiveMetadataSchemaDto from(DescriptiveMetadataSchema s) {
            return new DescriptiveMetadataSchemaDto(
                    s.id(),
                    s.entityType(),
                    s.schemaJson(),
                    s.active(),
                    s.createdAt(),
                    s.updatedAt());
        }
    }
}
