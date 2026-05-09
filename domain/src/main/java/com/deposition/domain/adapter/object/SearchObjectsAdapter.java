package com.deposition.domain.adapter.object;

import com.deposition.domain.port.in.object.ObjectSearchRequest;
import com.deposition.domain.port.in.object.SearchObjectsInPort;
import com.deposition.domain.port.in.object.SearchObjectsResult;
import com.deposition.domain.models.acl.AclPermission;
import com.deposition.domain.models.acl.AclRole;
import com.deposition.domain.port.out.ObjectSearchOutPort;
import com.deposition.domain.port.out.ObjectSearchFilters;
import com.deposition.domain.port.out.ObjectSearchQuery;
import com.deposition.domain.port.out.UserOutPort;
import com.deposition.domain.service.IntellectualEntityTypeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Validated
public class SearchObjectsAdapter implements SearchObjectsInPort {

    private final ObjectSearchOutPort searchOutPort;
    private final UserOutPort userOutPort;
    private final IntellectualEntityTypeResolver intellectualEntityTypeResolver;

    @Override
    public SearchObjectsResult search(ObjectSearchRequest request) {
        var userId = userOutPort.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("Unauthenticated request: cannot search objects");
        }

        var filters = new ObjectSearchFilters(
                userId,
                Set.of(AclPermission.READ),
                Set.of(AclRole.SUPER_ADMIN),
                Set.of(ObjectSearchFilters.Visibility.PUBLIC));

        var query = new ObjectSearchQuery(request, filters);
        var result = searchOutPort.search(query);

        // Enrich OpenSearch hits with full IntellectualEntityType (id/name/description).
        var enrichedHits = result.hits().stream()
                .map(h -> new SearchObjectsResult.Hit(
                        h.objectId(),
                        intellectualEntityTypeResolver.resolveByName(h.intellectualEntityType().name()),
                        h.originalName()))
                .toList();

        return new SearchObjectsResult(result.total(), enrichedHits);
    }

}
