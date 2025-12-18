package com.aciworldwide.exception;

import java.util.Map;

/**
 * Exception thrown when business logic validation fails.
 */
public class BusinessException extends AppException {

    public BusinessException(String message) {
        super(message, "BUSINESS_ERROR", 422);
    }

    public BusinessException(String message, Map<String, String> errorDetails) {
        super(message, "BUSINESS_ERROR", 422, errorDetails);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, "BUSINESS_ERROR", 422, cause);
    }
}
