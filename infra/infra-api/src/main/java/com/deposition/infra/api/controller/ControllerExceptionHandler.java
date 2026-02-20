package com.deposition.infra.api.controller;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler({
            MissingServletRequestPartException.class,
            MethodArgumentNotValidException.class,
            BindException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            ServletRequestBindingException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequestExceptions(Exception exception) {
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
}