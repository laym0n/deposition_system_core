package com.deposition.domain.adapter.object;

import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.deposition.domain.port.in.ObjectSearchRequest;
import com.deposition.domain.port.in.SearchObjectsInPort;
import com.deposition.domain.port.in.SearchObjectsResult;
import com.deposition.domain.port.out.ObjectSearchOutPort;
import com.deposition.domain.port.out.UserOutPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Validated
public class SearchObjectsAdapter implements SearchObjectsInPort {

    private final ObjectSearchOutPort searchOutPort;
    private final UserOutPort userOutPort;

    @Override
    public SearchObjectsResult search(ObjectSearchRequest request) {
        var userId = userOutPort.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("Unauthenticated request: cannot search objects");
        }

        return searchOutPort.search(userId, request);
    }

}
