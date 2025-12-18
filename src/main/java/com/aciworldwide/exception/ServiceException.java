package com.aciworldwide.exception;

/**
 * Exception thrown when service layer operations fail.
 */
public class ServiceException extends AppException {

    public ServiceException(String message) {
        super(message, "SERVICE_ERROR", 500);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, "SERVICE_ERROR", 500, cause);
    }

    public ServiceException(String message, String errorCode, int httpStatus) {
        super(message, errorCode, httpStatus);
    }

    public ServiceException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, errorCode, httpStatus, cause);
    }
}
