package com.deposition.infra.opensearch.adapter;

import com.deposition.domain.port.out.BlockchainTxLookupOutPort;
import com.deposition.infra.opensearch.config.OpenSearchProperties;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "integration.opensearch", name = "enabled", havingValue = "true")
public class OpenSearchBlockchainTxLookupAdapter implements BlockchainTxLookupOutPort {

    private final OpenSearchClient client;
    private final OpenSearchProperties properties;

    private static Optional<String> extractAnyTxId(java.util.Map<?, ?> source) {
        Object anchors = source.get("anchors");
        if (!(anchors instanceof java.util.List<?> list) || list.isEmpty()) {
            return Optional.empty();
        }
        for (Object a : list) {
            if (a instanceof java.util.Map<?, ?> am) {
                Object txId = am.get("blockchainTxId");
                if (txId != null && !txId.toString().isBlank()) {
                    return Optional.of(txId.toString());
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<String> extractTxIdByStorageVersionId(java.util.Map<?, ?> source, String storageVersionId) {
        if (storageVersionId == null || storageVersionId.isBlank()) {
            return Optional.empty();
        }

        Object anchors = source.get("anchors");
        if (!(anchors instanceof java.util.List<?> list) || list.isEmpty()) {
            return Optional.empty();
        }

        for (Object a : list) {
            if (!(a instanceof java.util.Map<?, ?> am)) {
                continue;
            }
            Object vid = am.get("storageVersionId");
            if (vid == null || !storageVersionId.equals(vid.toString())) {
                continue;
            }

            Object txId = am.get("blockchainTxId");
            if (txId == null || txId.toString().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(txId.toString());
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> findTxId(UUID objectId, @Nullable String storageVersionId) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }

        var bool = new BoolQuery.Builder();

        bool.filter(f -> f.ids(i -> i.values(objectId.toString())));

        if (storageVersionId != null) {
            bool.filter(f -> f.term(t -> t.field("anchors.storageVersionId.keyword")
                    .value(FieldValue.of(storageVersionId))));
        }

        var query = new Query.Builder().bool(bool.build()).build();

        try {
            var osRequest = new SearchRequest.Builder()
                    .index(properties.getObjectIndex())
                    .size(1)
                    .query(query)
                    .build();

            var response = client.search(osRequest, Object.class);
            if (response.hits() == null || response.hits().hits() == null || response.hits().hits().isEmpty()) {
                return Optional.empty();
            }

            var hit = response.hits().hits().get(0);
            if (!(hit.source() instanceof java.util.Map<?, ?> m)) {
                return Optional.empty();
            }

            if (storageVersionId == null) {
                return extractAnyTxId(m);
            }

            return extractTxIdByStorageVersionId(m, storageVersionId);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "OpenSearch txId lookup failed for objectId=" + objectId + ", storageVersionId="
                            + storageVersionId,
                    ex);
        }
    }
}
