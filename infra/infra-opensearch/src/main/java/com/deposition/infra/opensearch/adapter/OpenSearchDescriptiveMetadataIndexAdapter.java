package com.deposition.infra.opensearch.adapter;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.deposition.domain.port.in.schema.IntellectualEntityType;
import com.deposition.domain.port.out.DescriptiveMetadataIndexOutPort;
import com.deposition.infra.opensearch.config.OpenSearchProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "integration.opensearch", name = "enabled", havingValue = "true")
public class OpenSearchDescriptiveMetadataIndexAdapter implements DescriptiveMetadataIndexOutPort {

    private final OpenSearchClient client;
    private final OpenSearchProperties properties;

    @Override
    public void index(UUID intellectualEntityId, IntellectualEntityType entityType, Map<String, Object> extractedFields) {
        if (intellectualEntityId == null) {
            throw new IllegalArgumentException("intellectualEntityId must not be null");
        }
        if (entityType == null) {
            throw new IllegalArgumentException("entityType must not be null");
        }
        if (extractedFields == null) {
            throw new IllegalArgumentException("extractedFields must not be null");
        }

        var doc = new HashMap<String, Object>();
        doc.put("objectId", intellectualEntityId.toString());
        doc.put("entityType", entityType.name());
        doc.put("indexedAt", OffsetDateTime.now().toString());
        doc.putAll(extractedFields);

        try {
            client.index(i -> i
                    .index(properties.getDescriptiveMetadataIndex())
                    .id(intellectualEntityId.toString())
                    .document(doc));
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to index descriptive metadata in OpenSearch for objectId=" + intellectualEntityId,
                    ex);
        }
    }
}
