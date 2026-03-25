package com.deposition.infra.opensearch.adapter;

import com.deposition.domain.port.in.object.ObjectSearchRequest;
import com.deposition.domain.port.in.object.SearchObjectsResult;
import com.deposition.domain.port.out.ObjectSearchOutPort;
import com.deposition.infra.opensearch.config.OpenSearchProperties;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpenSearchObjectSearchAdapter implements ObjectSearchOutPort {

    static final List<String> FULL_TEXT_FIELDS = List.of("descriptive.*");

    private final OpenSearchClient client;
    private final OpenSearchProperties properties;

    private static void applyTxIdFilter(ObjectSearchRequest request, BoolQuery.Builder bool) {
        if (request.txId() == null || request.txId().isBlank()) {
            return;
        }

        bool.filter(f -> f.term(t -> t.field("anchors.blockchainTxId.keyword").value(FieldValue.of(request.txId()))));
    }

    private static void applyFullText(ObjectSearchRequest request, BoolQuery.Builder bool) {
        if (request.searchQuery() == null || request.searchQuery().isBlank()) {
            return;
        }

        bool.must(m -> m.simpleQueryString(sqs -> sqs
                .query(request.searchQuery())
                .fields(FULL_TEXT_FIELDS)
                .lenient(true)
                .analyzeWildcard(true)));
    }

    @Override
    public SearchObjectsResult search(String userId, ObjectSearchRequest request) {
        if (request == null) {
            request = new ObjectSearchRequest(null, null, 0, 50);
        }

        var bool = new BoolQuery.Builder();

        bool.filter(f -> f.term(t -> t.field("acl.entries.principal.id.keyword").value(FieldValue.of(userId))));

        // Index contains only intellectual entities.
        bool.filter(f -> f.term(t -> t.field("entityType.keyword").value(FieldValue.of("INTELLECTUAL_ENTITY"))));

        applyTxIdFilter(request, bool);
        applyFullText(request, bool);

        var finalQuery = new Query.Builder().bool(bool.build()).build();

        try {
            var osRequest = new SearchRequest.Builder()
                    .index(properties.getObjectIndex())
                    .from(request.offset())
                    .size(request.effectiveLimit())
                    .query(finalQuery);

            SearchResponse<Object> response = client.search(osRequest.build(), Object.class);

            var hits = response.hits() == null ? List.<SearchObjectsResult.Hit>of()
                    : response.hits().hits().stream()
                    .map(h -> {
                        if (h == null || h.id() == null) {
                            return null;
                        }
                        UUID id;
                        try {
                            id = UUID.fromString(h.id());
                        } catch (IllegalArgumentException ex) {
                            return null;
                        }

                        String entityType = null;
                        if (h.source() instanceof java.util.Map<?, ?> m) {
                            Object et = m.get("entityType");
                            entityType = et == null ? null : et.toString();
                        }

                        if (entityType == null || entityType.isBlank()) {
                            entityType = "UNKNOWN";
                        }
                        return new SearchObjectsResult.Hit(id, entityType);
                    })
                    .filter(Objects::nonNull)
                    .toList();

            long total = response.hits() != null && response.hits().total() != null
                    ? response.hits().total().value()
                    : hits.size();

            return new SearchObjectsResult(total, hits);
        } catch (Exception ex) {
            throw new IllegalStateException("OpenSearch query failed", ex);
        }
    }
}
