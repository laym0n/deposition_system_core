package com.deposition.infra.api.controller.common;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.deposition.domain.exception.DescriptiveMetadataSchemaNotFoundException;
import com.deposition.domain.exception.DescriptiveMetadataValidationException;
import com.deposition.domain.exception.ObjectAccessDeniedException;
import com.deposition.domain.exception.ObjectNotFoundException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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

    @ExceptionHandler({
        MissingServletRequestPartException.class,
        MethodArgumentNotValidException.class,
        BindException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        ServletRequestBindingException.class,
        ConstraintViolationException.class,
        IllegalArgumentException.class,
        DescriptiveMetadataSchemaNotFoundException.class,
        DescriptiveMetadataValidationException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequestExceptions(Exception exception) {
        if (exception instanceof DescriptiveMetadataValidationException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "reason", resolveReason(exception),
                            "errors", String.join("; ", ex.getErrors())));
        }

        return ResponseEntity.badRequest().body(Map.of("reason", resolveReason(exception)));
    }

    private String resolveReason(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException ex && ex.getBindingResult().hasErrors()) {
            return ex.getBindingResult().getAllErrors().stream()
                    .map(error -> Optional.ofNullable(error.getDefaultMessage()).orElse(error.toString()))
                    .collect(Collectors.joining("; "));
        }

        if (exception instanceof BindException ex && ex.getBindingResult().hasErrors()) {
            return ex.getBindingResult().getAllErrors().stream()
                    .map(error -> Optional.ofNullable(error.getDefaultMessage()).orElse(error.toString()))
                    .collect(Collectors.joining("; "));
        }

        return Optional.ofNullable(exception.getMessage())
                .filter(message -> !message.isBlank())
                .orElse("Bad request");
    }

    public record ApiError(String code, String message) {

    }
}
