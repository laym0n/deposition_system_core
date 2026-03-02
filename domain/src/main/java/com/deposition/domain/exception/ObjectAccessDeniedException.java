package com.deposition.domain.exception;

import java.util.UUID;

public class ObjectAccessDeniedException extends RuntimeException {

    public ObjectAccessDeniedException(UUID objectId) {
        super("Object is not accessible for current user. objectId=" + objectId);
    }
}
