package com.aciworldwide.exception.mapper;

import com.aciworldwide.dto.GenericApiResponse;
import com.aciworldwide.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Maps exceptions to GenericApiResponse objects.
 * Provides consistent error response structure across the application.
 */
@Slf4j
@Component
public class ExceptionMapper {

    /**
     * Maps an AppException to a GenericApiResponse.
     *
     * @param exception the application exception
     * @return mapped response with error details
     */
    public GenericApiResponse<Void> mapAppException(AppException exception) {
        log.error("Application exception occurred: {} - {}", exception.getErrorCode(), exception.getMessage(), exception);
        
        if (exception.getErrorDetails() != null && !exception.getErrorDetails().isEmpty()) {
            return GenericApiResponse.error(exception.getMessage(), exception.getErrorDetails(), exception.getHttpStatus());
        }
        
        return GenericApiResponse.error(exception.getMessage(), exception.getHttpStatus());
    }

    /**
     * Maps a generic RuntimeException to a GenericApiResponse.
     *
     * @param exception the runtime exception
     * @return mapped response with generic error message
     */
    public GenericApiResponse<Void> mapRuntimeException(RuntimeException exception) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unexpected runtime exception occurred [errorId={}]: {}", errorId, exception.getMessage(), exception);
        
        return GenericApiResponse.error(
            "An unexpected error occurred. Please contact support with error ID: " + errorId,
            Map.of("errorId", errorId, "type", exception.getClass().getSimpleName()),
            500
        );
    }

    /**
     * Maps a generic Exception to a GenericApiResponse.
     *
     * @param exception the exception
     * @return mapped response with generic error message
     */
    public GenericApiResponse<Void> mapGenericException(Exception exception) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unexpected exception occurred [errorId={}]: {}", errorId, exception.getMessage(), exception);
        
        return GenericApiResponse.error(
            "An unexpected error occurred. Please contact support with error ID: " + errorId,
            Map.of("errorId", errorId, "type", exception.getClass().getSimpleName()),
            500
        );
    }

    /**
     * Maps validation errors to a GenericApiResponse.
     *
     * @param validationErrors map of field validation errors
     * @return mapped response with validation error details
     */
    public GenericApiResponse<Void> mapValidationErrors(Map<String, String> validationErrors) {
        log.warn("Validation errors occurred: {}", validationErrors);
        return GenericApiResponse.error("Validation failed", validationErrors, 400);
    }

    /**
     * Gets the HTTP status code from an exception.
     *
     * @param exception the exception
     * @return HTTP status code
     */
    public int getHttpStatusCode(Throwable exception) {
        if (exception instanceof AppException appException) {
            return appException.getHttpStatus();
        }
        return 500; // Internal Server Error for unknown exceptions
    }
}
