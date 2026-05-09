package com.deposition.infra.opensearch.adapter;

import com.deposition.domain.port.in.object.ObjectSearchRequest;
import com.deposition.domain.port.in.object.SearchObjectsResult;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectSearchOutPort;
import com.deposition.domain.port.out.ObjectSearchQuery;
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

    static final List<String> FULL_TEXT_FIELDS = List.of(
            "descriptive.*",
            "anchors.*",
            "premis.**");

    private final OpenSearchClient client;
    private final OpenSearchProperties properties;

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
    public SearchObjectsResult search(ObjectSearchQuery query) {
        if (query == null || query.request() == null || query.filters() == null) {
            throw new IllegalArgumentException("query, query.request and query.filters must not be null");
        }

        var request = query.request();
        var filters = query.filters();

        var bool = new BoolQuery.Builder();

        String principalId = filters.principalId();

        var access = new BoolQuery.Builder();
        boolean hasAccessRule = false;

        if (filters.anyVisibility() != null && !filters.anyVisibility().isEmpty()) {
            hasAccessRule = true;
            access.should(s -> s.terms(t -> t
                    .field("visibility.keyword")
                    .terms(v -> v.value(filters.anyVisibility().stream()
                            .filter(Objects::nonNull)
                            .map(vv -> FieldValue.of(vv.name()))
                            .toList()))));
        }

        if (filters.anyAclPermissions() != null && !filters.anyAclPermissions().isEmpty()) {
            for (var p : filters.anyAclPermissions()) {
                if (p == null) {
                    continue;
                }
                hasAccessRule = true;
                access.should(s -> s.bool(b -> b
                        .filter(f -> f.term(t -> t.field("acl.entries.principal.id.keyword")
                                .value(FieldValue.of(principalId))))
                        .filter(f -> f.term(t -> t.field("acl.entries.permissions.keyword")
                                .value(FieldValue.of(p.name()))))));
            }
        }

        if (filters.anyAclRoles() != null && !filters.anyAclRoles().isEmpty()) {
            for (var r : filters.anyAclRoles()) {
                if (r == null) {
                    continue;
                }
                hasAccessRule = true;
                access.should(s -> s.bool(b -> b
                        .filter(f -> f.term(t -> t.field("acl.entries.principal.id.keyword")
                                .value(FieldValue.of(principalId))))
                        .filter(f -> f.term(t -> t.field("acl.entries.role.keyword")
                                .value(FieldValue.of(r.name()))))));
            }
        }

        if (!hasAccessRule) {
            bool.must(m -> m.term(t -> t.field("__deny").value(FieldValue.of("1"))));
        } else {
            bool.filter(f -> f.bool(access.minimumShouldMatch("1").build()));
        }

        applyFullText(request, bool);

        var finalQuery = new Query.Builder().bool(bool.build()).build();

        try {
            var osRequest = new SearchRequest.Builder()
                    .index(properties.getObjectIndex())
                    .from(request.offset())
                    .size(request.effectiveLimit())
                    .query(finalQuery);

            SearchResponse<ObjectIndexDocument> response = client.search(osRequest.build(), ObjectIndexDocument.class);

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
                        var source = h.source();
                        if (source == null) {
                            return null;
                        }

                        var type = source.intellectualEntityType();
                        var originalName = source.premis() == null ? null : source.premis().originalName();

                        return new SearchObjectsResult.Hit(id, type, originalName);
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
