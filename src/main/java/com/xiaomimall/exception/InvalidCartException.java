package com.xiaomimall.exception;

// InvalidCartException.java
public class InvalidCartException extends RuntimeException {
    public InvalidCartException(String message) {
        super(message);
    }
}