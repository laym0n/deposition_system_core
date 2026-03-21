package com.deposition.domain.exception;

public class ResourceNotFoundException extends ModuleException {

    public ResourceNotFoundException(String resource, String id) {
        super(resource + "not found by " + id);
    }
    public ResourceNotFoundException(String resource) {
        super(resource + "not found");
    }
}
