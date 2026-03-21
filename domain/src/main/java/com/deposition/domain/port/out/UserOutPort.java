package com.deposition.domain.port.out;

import java.util.Optional;

import com.deposition.domain.exception.ResourceNotFoundException;

public interface UserOutPort {

    Optional<String> getOptinalCurrentUserId();

    default String getCurrentUserId() {
        return getOptinalCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("AuthenticatedUser"));
    }
}
