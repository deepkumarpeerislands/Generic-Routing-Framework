package com.aciworldwide.exception;

import com.aciworldwide.dto.GenericApiResponse;
import com.aciworldwide.exception.handler.GlobalExceptionHandler;
import com.aciworldwide.exception.mapper.ExceptionMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class demonstrating the global exception handler framework.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private RoutingContext context;
    
    @Mock
    private io.vertx.core.http.HttpServerResponse response;
    
    private ExceptionMapper exceptionMapper;
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionMapper = new ExceptionMapper();
        globalExceptionHandler = new GlobalExceptionHandler(exceptionMapper);
        
        when(context.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
        when(response.ended()).thenReturn(false);
    }

    @Test
    void testValidationExceptionHandling() {
        // Given
        ValidationException exception = new ValidationException("Invalid input", 
            Map.of("field", "email", "error", "Invalid format"));
        when(context.failure()).thenReturn(exception);

        // When
        globalExceptionHandler.handleFailure(context);

        // Then
        verify(response).setStatusCode(400);
        verify(response).putHeader("Content-Type", "application/json");
        verify(response).end(any(String.class));
    }

    @Test
    void testBusinessExceptionHandling() {
        // Given
        BusinessException exception = new BusinessException("Business rule violated",
            Map.of("rule", "max_attempts", "current", "5"));
        when(context.failure()).thenReturn(exception);

        // When
        globalExceptionHandler.handleFailure(context);

        // Then
        verify(response).setStatusCode(422);
        verify(response).putHeader("Content-Type", "application/json");
        verify(response).end(any(String.class));
    }

    @Test
    void testResourceNotFoundExceptionHandling() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("User", "123");
        when(context.failure()).thenReturn(exception);

        // When
        globalExceptionHandler.handleFailure(context);

        // Then
        verify(response).setStatusCode(404);
        verify(response).putHeader("Content-Type", "application/json");
        verify(response).end(any(String.class));
    }

    @Test
    void testDatabaseExceptionHandling() {
        // Given
        DatabaseException exception = new DatabaseException("Connection timeout");
        when(context.failure()).thenReturn(exception);

        // When
        globalExceptionHandler.handleFailure(context);

        // Then
        verify(response).setStatusCode(500);
        verify(response).putHeader("Content-Type", "application/json");
        verify(response).end(any(String.class));
    }

    @Test
    void testServiceExceptionHandling() {
        // Given
        ServiceException exception = new ServiceException("External service unavailable",
            "EXTERNAL_SERVICE_ERROR", 503);
        when(context.failure()).thenReturn(exception);

        // When
        globalExceptionHandler.handleFailure(context);

        // Then
        verify(response).setStatusCode(503);
        verify(response).putHeader("Content-Type", "application/json");
        verify(response).end(any(String.class));
    }

    @Test
    void testRuntimeExceptionHandling() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");
        when(context.failure()).thenReturn(exception);

        // When
        globalExceptionHandler.handleFailure(context);

        // Then
        verify(response).setStatusCode(500);
        verify(response).putHeader("Content-Type", "application/json");
        verify(response).end(any(String.class));
    }

    @Test
    void testGenericExceptionHandling() {
        // Given
        Exception exception = new Exception("Generic error");
        when(context.failure()).thenReturn(exception);

        // When
        globalExceptionHandler.handleFailure(context);

        // Then
        verify(response).setStatusCode(500);
        verify(response).putHeader("Content-Type", "application/json");
        verify(response).end(any(String.class));
    }

    @Test
    void testUnknownFailureHandling() {
        // Given
        when(context.failure()).thenReturn(null);

        // When
        globalExceptionHandler.handleFailure(context);

        // Then
        verify(response).setStatusCode(500);
        verify(response).putHeader("Content-Type", "application/json");
        verify(response).end(any(String.class));
    }

    @Test
    void testResponseAlreadyEnded() {
        // Given
        ValidationException exception = new ValidationException("Test error");
        when(context.failure()).thenReturn(exception);
        when(response.ended()).thenReturn(true);

        // When
        globalExceptionHandler.handleFailure(context);

        // Then
        verify(response, never()).setStatusCode(anyInt());
        verify(response, never()).end(any(String.class));
    }

    @Test
    void testFailContextUtilityMethods() {
        // Test static utility methods
        Exception testException = new ValidationException("Test validation error");
        
        // These methods should not throw exceptions
        GlobalExceptionHandler.failContext(context, testException);
        GlobalExceptionHandler.failContext(context, 400, testException);
        
        verify(context).fail(testException);
        verify(context).fail(400, testException);
    }
}
