package com.deposition.domain.port.out;

import java.util.Optional;

public interface UserService {

    Optional<String> getCurrentUserId();
}
