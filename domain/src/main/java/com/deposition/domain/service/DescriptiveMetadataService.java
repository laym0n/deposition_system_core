package com.deposition.domain.service;

import com.deposition.domain.exception.DescriptiveMetadataValidationException;
import com.deposition.domain.exception.ResourceNotFoundException;
import com.deposition.domain.models.IntellectualEntityType;
import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DescriptiveMetadataService {

    private static final String DESCRIPTIVE_METADATA_FILENAME = "descriptive-metadata.json";

    private final DescriptiveMetadataSchemaOutPort schemaOutPort;
    private final FileStorageOutPort fileStorage;
    private final ObjectMapper objectMapper;

    private static JsonSchema loadSchema(JsonNode schemaNode) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        return factory.getSchema(schemaNode);
    }

    public Map<String, Object> validateAndPersistIfPresent(UUID intellectualEntityId,
                                                           IntellectualEntityType entityType,
                                                           String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return Map.of();
        }

        var schemaJson = schemaOutPort.findActiveSchemaJsonByEntityType(entityType.name())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DescriptiveMetadataSchema", "entityType=" + entityType));

        try {
            JsonNode schemaNode = objectMapper.readTree(schemaJson);
            JsonSchema schema = loadSchema(schemaNode);
            JsonNode metadataNode = objectMapper.readTree(metadataJson);
            var errors = schema.validate(metadataNode);
            if (!errors.isEmpty()) {
                throw new DescriptiveMetadataValidationException(
                        "Descriptive metadata validation failed for entityType=" + entityType,
                        errors.stream().map(ValidationMessage::getMessage).toList());
            }

            persistJson(intellectualEntityId, metadataJson);
            var extractedFields = extractFields(metadataNode, schemaNode);
            return extractedFields;
        } catch (DescriptiveMetadataValidationException ex) {
            throw ex;
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid descriptiveMetadata JSON: " + ex.getOriginalMessage(), ex);
        } catch (RuntimeException ex) {
            throw new DescriptiveMetadataValidationException(
                    "Descriptive metadata validation failed for entityType=" + entityType,
                    List.of(ex.getMessage()));
        }
    }

    private void persistJson(UUID intellectualEntityId, String json) {
        try {
            var bytes = json.getBytes(StandardCharsets.UTF_8);
            Resource resource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return DESCRIPTIVE_METADATA_FILENAME;
                }
            };
            fileStorage.persist(resource, intellectualEntityId.toString());
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to persist descriptive metadata for objectId=" + intellectualEntityId,
                    ex);
        }
    }

    @SneakyThrows
    private Map<String, Object> extractFields(JsonNode metadataNode, JsonNode schemaNode) {
        if (metadataNode == null || metadataNode.isNull() || metadataNode.isMissingNode()) {
            return Map.of();
        }
        return objectMapper.convertValue(metadataNode, new TypeReference<Map<String, Object>>() {
        });
    }
}
