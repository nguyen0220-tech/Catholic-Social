package com.catholic.ac.kr.catholicsocial.exception;

public class ResourceNotFoudException extends RuntimeException {
    public ResourceNotFoudException() {
        super();
    }

    public ResourceNotFoudException(String message) {
        super(message);
    }

    public ResourceNotFoudException(String message, Throwable cause) {
        super(message, cause);
    }
}
