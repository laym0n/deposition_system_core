package com.deposition.domain.exception;

import java.util.List;

public class DescriptiveMetadataValidationException extends ModuleException {

    private final List<String> errors;

    public DescriptiveMetadataValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
