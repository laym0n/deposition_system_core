package com.deposition.domain.adapter.object;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.port.in.ObjectSearchRequest;
import com.deposition.domain.port.in.SearchObjectsInPort;
import com.deposition.domain.port.in.SearchObjectsResult;
import com.deposition.domain.port.out.ObjectSearchOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class SearchObjectsAdapter implements SearchObjectsInPort {

    private final ObjectSearchOutPort searchOutPort;

    @Override
    public SearchObjectsResult search(ObjectSearchRequest request) {
        var userId = resolveCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("Unauthenticated request: cannot search objects");
        }

        return searchOutPort.search(userId, request);
    }

    private static String resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }
}
