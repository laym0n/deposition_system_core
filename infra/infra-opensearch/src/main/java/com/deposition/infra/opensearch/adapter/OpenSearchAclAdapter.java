package com.deposition.infra.opensearch.adapter;

import com.deposition.domain.models.acl.ObjectAcl;
import com.deposition.domain.port.out.AclOutPort;
import com.deposition.domain.port.out.ObjectIndexDocument;
import com.deposition.infra.opensearch.config.OpenSearchProperties;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpenSearchAclAdapter implements AclOutPort {

    private final OpenSearchClient client;
    private final OpenSearchProperties properties;

    @Override
    public Optional<ObjectAcl> findByObjectId(UUID objectId) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }

        try {
            var getRequest = new GetRequest.Builder()
                    .index(properties.getObjectIndex())
                    .id(objectId.toString())
                    .build();

            var response = client.get(getRequest, ObjectIndexDocument.class);
            if (!response.found() || response.source() == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.source().acl());
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "OpenSearch ACL lookup failed for objectId=" + objectId,
                    ex);
        }
    }

    @Override
    public ObjectAcl save(ObjectAcl acl) {
        if (acl == null || acl.getObjectId() == null) {
            throw new IllegalArgumentException("ACL or objectId must not be null");
        }

        UUID objectId = acl.getObjectId();

        try {
            ObjectIndexDocument existing;
            var getRequest = new GetRequest.Builder()
                    .index(properties.getObjectIndex())
                    .id(objectId.toString())
                    .build();
            var response = client.get(getRequest, ObjectIndexDocument.class);
            if (!response.found() || response.source() == null) {
                throw new IllegalStateException(
                        "Cannot persist ACL: object index document not found in OpenSearch for objectId=" + objectId);
            }
            existing = response.source();

            // Re-index the whole document to avoid losing any fields.
            var updated = new ObjectIndexDocument(
                    existing.objectId(),
                    existing.intellectualEntityType(),
                    acl,
                    existing.anchors(),
                    existing.visibility(),
                    existing.premis(),
                    existing.descriptive());

            client.index(i -> i
                    .index(properties.getObjectIndex())
                    .id(objectId.toString())
                    .document(updated));

            return acl;
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to persist ACL in OpenSearch for objectId=" + objectId,
                    ex);
        }
    }
}
