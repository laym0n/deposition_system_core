package com.deposition.domain.port.out;

import com.deposition.domain.exception.ResourceNotFoundException;

import java.util.Optional;

public interface UserOutPort {

    Optional<String> getOptinalCurrentUserId();

    default String getCurrentUserId() {
        return getOptinalCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("AuthenticatedUser"));
    }
}
