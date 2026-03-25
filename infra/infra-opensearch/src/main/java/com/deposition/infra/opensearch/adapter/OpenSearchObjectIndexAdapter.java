package com.deposition.infra.opensearch.adapter;

import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.domain.port.out.ObjectIndexOutPort;
import com.deposition.infra.opensearch.config.OpenSearchProperties;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetRequest;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenSearchObjectIndexAdapter implements ObjectIndexOutPort {

    private final OpenSearchClient client;
    private final OpenSearchProperties properties;

    private static List<ObjectIndexDocument.Anchor> mergeAnchors(
            List<ObjectIndexDocument.Anchor> existing,
            List<ObjectIndexDocument.Anchor> incoming) {
        if ((incoming == null || incoming.isEmpty()) && (existing == null || existing.isEmpty())) {
            return null;
        }

        List<ObjectIndexDocument.Anchor> merged = new ArrayList<>();
        if (existing != null) {
            merged.addAll(existing);
        }

        if (incoming != null) {
            for (var a : incoming) {
                if (a == null) {
                    continue;
                }
                if ((a.storageVersionId() == null || a.storageVersionId().isBlank())
                        && (a.blockchainTxId() == null || a.blockchainTxId().isBlank())) {
                    continue;
                }

                boolean alreadyExists = merged.stream().anyMatch(e -> e != null
                        && eq(e.storageVersionId(), a.storageVersionId())
                        && eq(e.blockchainTxId(), a.blockchainTxId()));
                if (alreadyExists) {
                    continue;
                }

                merged.add(new ObjectIndexDocument.Anchor(
                        blankToNull(a.storageVersionId()),
                        blankToNull(a.blockchainTxId()),
                        a.anchoredAt() == null ? ZonedDateTime.now() : a.anchoredAt()));
            }
        }

        return merged;
    }

    private static boolean eq(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    @Override
    public void index(ObjectIndexDocument document) {
        try {
            // Merge anchors history (multiple {versionId, txId}) into a single document.
            ObjectIndexDocument merged = mergeWithExisting(document);

            client.index(i -> i
                    .index(properties.getObjectIndex())
                    .id(document.objectId().toString())
                    .document(merged));
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to index object in OpenSearch for objectId=" + document.objectId(),
                    ex);
        }
    }

    private ObjectIndexDocument mergeWithExisting(ObjectIndexDocument incoming) {
        ObjectIndexDocument existing = null;
        try {
            var getRequest = new GetRequest.Builder()
                    .index(properties.getObjectIndex())
                    .id(incoming.objectId().toString())
                    .build();

            var response = client.get(getRequest, ObjectIndexDocument.class);
            if (response.found()) {
                existing = response.source();
            }
        } catch (Exception ex) {
            // Treat lookup failures as "document does not exist" (index will still proceed).
            existing = null;
        }

        List<ObjectIndexDocument.Anchor> mergedAnchors = mergeAnchors(
                existing == null ? null : existing.anchors(),
                incoming.anchors());

        return new ObjectIndexDocument(
                incoming.objectId(),
                incoming.acl(),
                incoming.originalName(),
                mergedAnchors,
                incoming.identifiers(),
                incoming.relationships(),
                incoming.descriptive());
    }
}
