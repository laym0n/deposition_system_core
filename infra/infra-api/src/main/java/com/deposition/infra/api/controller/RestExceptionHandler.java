package com.deposition.infra.api.controller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.deposition.domain.exception.ObjectAccessDeniedException;
import com.deposition.domain.exception.ObjectNotFoundException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ObjectNotFoundException ex) {
        log.error(ExceptionUtils.getMessage(ex), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(ObjectAccessDeniedException.class)
    public ResponseEntity<ApiError> handleForbidden(ObjectAccessDeniedException ex) {
        log.error(ExceptionUtils.getMessage(ex), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("FORBIDDEN", ex.getMessage()));
    }

    public record ApiError(String code, String message) {

    }
}
