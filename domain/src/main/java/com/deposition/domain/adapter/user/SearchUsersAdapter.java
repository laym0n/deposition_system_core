package com.deposition.domain.adapter.user;

import com.deposition.domain.port.in.user.SearchUsersInPort;
import com.deposition.domain.port.in.user.SearchUsersRequest;
import com.deposition.domain.port.out.UserDirectoryOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@RequiredArgsConstructor
@Validated
public class SearchUsersAdapter implements SearchUsersInPort {

    private final UserDirectoryOutPort userDirectoryOutPort;

    @Override
    public List<UserSummary> search(SearchUsersRequest request) {
        var query = new UserDirectoryOutPort.UserSearchQuery(
                request.searchQuery(),
                request.offset(),
                request.effectiveLimit());

        return userDirectoryOutPort.search(query)
                .stream()
                .map(u -> new UserSummary(u.id(), u.username()))
                .toList();
    }
}
