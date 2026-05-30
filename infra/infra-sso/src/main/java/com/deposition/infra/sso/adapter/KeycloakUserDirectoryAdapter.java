package com.deposition.infra.sso.adapter;

import com.deposition.domain.port.out.UserDirectoryOutPort;
import com.deposition.infra.sso.client.KeycloakAdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KeycloakUserDirectoryAdapter implements UserDirectoryOutPort {

    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    public List<UserSummary> search(UserSearchQuery query) {
        if (!StringUtils.hasText(query.searchQuery())) {
            return Collections.emptyList();
        }

        return keycloakAdminClient.searchUsers(query.searchQuery(), query.offset(), query.limit())
                .stream()
                .filter(u -> u.id() != null && u.username() != null)
                .map(u -> new UserSummary(u.id(), u.username()))
                .toList();
    }
}
