package com.deposition.infra.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.deposition.domain.exception.ObjectAccessDeniedException;
import com.deposition.domain.exception.ObjectNotFoundException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ObjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ObjectAccessDeniedException.class)
    public ResponseEntity<ApiError> handleForbidden(ObjectAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("FORBIDDEN", ex.getMessage()));
    }

    public record ApiError(String code, String message) {

    }
}
