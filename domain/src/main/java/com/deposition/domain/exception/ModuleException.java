package com.deposition.domain.exception;

public class ModuleException extends RuntimeException {

    public ModuleException() {
        super();
    }
    public ModuleException(Throwable throwable) {
        super(throwable);
    }
    public ModuleException(String msg) {
        super(msg);
    }
    public ModuleException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
