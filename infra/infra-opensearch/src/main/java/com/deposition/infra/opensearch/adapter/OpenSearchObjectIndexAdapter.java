package com.deposition.infra.opensearch.adapter;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.stereotype.Component;

import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexOutPort;
import com.deposition.infra.opensearch.config.OpenSearchProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenSearchObjectIndexAdapter implements ObjectIndexOutPort {

    private final OpenSearchClient client;
    private final OpenSearchProperties properties;

    @Override
    public void index(ObjectIndexDocument document) {
        try {
            client.index(i -> i
                    .index(properties.getObjectIndex())
                    .id(document.objectId().toString())
                    .document(document));
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to index object in OpenSearch for objectId=" + document.objectId()
                    + ", entityType=" + document.entityType(),
                    ex);
        }
    }
}
