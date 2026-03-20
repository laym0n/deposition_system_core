package com.deposition.infra.opensearch.adapter;

import java.util.Optional;
import java.util.UUID;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexLookupOutPort;
import com.deposition.infra.opensearch.config.OpenSearchProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "integration.opensearch", name = "enabled", havingValue = "true")
public class OpenSearchObjectIndexLookupAdapter implements ObjectIndexLookupOutPort {

    private final OpenSearchClient client;
    private final OpenSearchProperties properties;

    @Override
    public Optional<ObjectIndexDocument> findByObjectId(UUID objectId) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }

        try {
            var getRequest = new GetRequest.Builder()
                    .index(properties.getObjectIndex())
                    .id(objectId.toString())
                    .build();

            var response = client.get(getRequest, ObjectIndexDocument.class);
            if (!response.found()) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.source());
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "OpenSearch lookup failed for objectId=" + objectId,
                    ex);
        }
    }
}
