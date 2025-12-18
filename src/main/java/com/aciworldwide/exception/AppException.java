package com.aciworldwide.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Base application exception for the Generic Routing Framework.
 * All custom exceptions should extend this class.
 */
@Getter
public abstract class AppException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;
    private final Map<String, String> errorDetails;

    protected AppException(String message, String errorCode, int httpStatus) {
        this(message, errorCode, httpStatus, null, null);
    }

    protected AppException(String message, String errorCode, int httpStatus, Throwable cause) {
        this(message, errorCode, httpStatus, cause, null);
    }

    protected AppException(String message, String errorCode, int httpStatus, Map<String, String> validationErrors) {
        this(message, errorCode, httpStatus, null, validationErrors);
    }

    protected AppException(String message, String errorCode, int httpStatus, Throwable cause, Map<String, String> errorDetails) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.errorDetails = errorDetails;
    }
}
