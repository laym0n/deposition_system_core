package com.deposition.infra.api.controller.user;

import com.deposition.domain.port.in.user.SearchUsersInPort;
import com.deposition.domain.port.in.user.SearchUsersRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final SearchUsersInPort searchUsersInPort;

    @GetMapping(value = "/users/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserSummaryResponse>> searchUsers(
            @RequestParam(name = "searchQuery", required = false) String searchQuery,
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false) Integer limit) {

        var request = new SearchUsersRequest(searchQuery, offset, limit);
        var users = searchUsersInPort.search(request);

        var response = users.stream()
                .map(u -> new UserSummaryResponse(u.id(), u.username()))
                .toList();

        return ResponseEntity.ok(response);
    }

    public record UserSummaryResponse(String id, String username) {
    }
}
