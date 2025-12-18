package com.aciworldwide.exception;

/**
 * Exception thrown when database operations fail.
 */
public class DatabaseException extends AppException {

    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR", 500);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, "DATABASE_ERROR", 500, cause);
    }
}
