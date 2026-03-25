package com.deposition.application.service;

import com.deposition.domain.port.out.UserOutPort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityContextUserService implements UserOutPort {

    @Override
    public Optional<String> getOptinalCurrentUserId() {
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
