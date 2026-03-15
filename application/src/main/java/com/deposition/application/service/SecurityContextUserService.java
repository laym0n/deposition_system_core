package com.deposition.application.service;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.deposition.domain.port.out.UserService;

@Component
public class SecurityContextUserService implements UserService {

    @Override
    public Optional<String> getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        var name = authentication.getName();
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(name);
    }
}
