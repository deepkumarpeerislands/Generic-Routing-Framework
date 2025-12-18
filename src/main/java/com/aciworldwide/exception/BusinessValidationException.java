package com.aciworldwide.exception;

import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
public class BusinessValidationException extends AppException {

    public BusinessValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
    }

    public BusinessValidationException(String message, Map<String, String> validationErrors) {
        super(message, "VALIDATION_ERROR", 400, validationErrors);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(message, "VALIDATION_ERROR", 400, cause);
    }
}
