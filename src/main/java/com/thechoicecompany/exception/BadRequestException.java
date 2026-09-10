// src/main/java/com/thechoicecompany/exception/BadRequestException.java
package com.thechoicecompany.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}