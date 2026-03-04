package com.deposition.domain.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.deposition.domain.exception.DescriptiveMetadataSchemaNotFoundException;
import com.deposition.domain.exception.DescriptiveMetadataValidationException;
import com.deposition.domain.port.in.IntellectualEntityType;
import com.deposition.domain.port.out.DescriptiveMetadataIndexOutPort;
import com.deposition.domain.port.out.DescriptiveMetadataSchemaOutPort;
import com.deposition.domain.port.out.FileStorageOutPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DescriptiveMetadataService {

    private static final String DESCRIPTIVE_METADATA_FILENAME = "descriptive-metadata.json";

    private final DescriptiveMetadataSchemaOutPort schemaOutPort;
    private final FileStorageOutPort fileStorage;
    private final DescriptiveMetadataIndexOutPort indexOutPort;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * If metadataJson is provided, validates it against JSON schema resolved by
     * entityType and persists the JSON in S3.
     */
    public void validateAndPersistIfPresent(UUID intellectualEntityId,
            IntellectualEntityType entityType,
            String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return;
        }
        if (intellectualEntityId == null) {
            throw new IllegalArgumentException("intellectualEntityId must not be null");
        }
        if (entityType == null) {
            throw new IllegalArgumentException("entityType must not be null");
        }

        var schemaJson = schemaOutPort.findActiveSchemaJsonByEntityType(entityType.name())
                .orElseThrow(() -> new DescriptiveMetadataSchemaNotFoundException(
                "Descriptive metadata JsonSchema not found for entityType=" + entityType));

        try {
            JsonSchema schema = loadSchema(schemaJson);
            JsonNode metadataNode = OBJECT_MAPPER.readTree(metadataJson);
            var errors = schema.validate(metadataNode);
            if (!errors.isEmpty()) {
                throw new DescriptiveMetadataValidationException(
                        "Descriptive metadata validation failed for entityType=" + entityType,
                        errors.stream().map(ValidationMessage::getMessage).toList());
            }

            // index extracted fields for search
            var extractedFields = extractFields(metadataNode);
            indexOutPort.index(intellectualEntityId, entityType, extractedFields);
        } catch (DescriptiveMetadataValidationException ex) {
            throw ex;
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid descriptiveMetadata JSON: " + ex.getOriginalMessage(), ex);
        } catch (RuntimeException ex) {
            throw new DescriptiveMetadataValidationException(
                    "Descriptive metadata validation failed for entityType=" + entityType,
                    List.of(ex.getMessage()));
        }

        persistJson(intellectualEntityId, metadataJson);
    }

    private static JsonSchema loadSchema(String schemaJson) throws com.fasterxml.jackson.core.JsonProcessingException {
        JsonNode schemaNode = OBJECT_MAPPER.readTree(schemaJson);
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        return factory.getSchema(schemaNode);
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
            throw new IllegalStateException("Failed to persist descriptive metadata for objectId=" + intellectualEntityId,
                    ex);
        }
    }

    private static java.util.Map<String, Object> extractFields(JsonNode metadataNode) {
        if (metadataNode == null || metadataNode.isNull() || metadataNode.isMissingNode()) {
            return java.util.Map.of();
        }

        var result = new java.util.HashMap<String, Object>();
        metadataNode.properties().forEach(entry -> {
            var key = entry.getKey();
            var value = entry.getValue();
            if (value == null || value.isNull() || value.isMissingNode()) {
                return;
            }
            if (value.isTextual()) {
                result.put(key, value.asText());
                return;
            }
            if (value.isNumber()) {
                result.put(key, value.numberValue());
                return;
            }
            if (value.isBoolean()) {
                result.put(key, value.asBoolean());
                return;
            }
            if (value.isArray()) {
                var list = new java.util.ArrayList<Object>();
                value.elements().forEachRemaining(element -> {
                    if (element == null || element.isNull()) {
                        return;
                    }
                    if (element.isTextual()) {
                        list.add(element.asText());
                    } else if (element.isNumber()) {
                        list.add(element.numberValue());
                    } else if (element.isBoolean()) {
                        list.add(element.asBoolean());
                    } else {
                        // fallback: store json string for complex elements
                        list.add(element.toString());
                    }
                });
                result.put(key, list);
                return;
            }
            result.put(key, value.toString());
        });
        return java.util.Collections.unmodifiableMap(result);
    }
}
