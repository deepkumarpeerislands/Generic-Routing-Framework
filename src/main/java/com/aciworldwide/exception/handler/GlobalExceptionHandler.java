package com.aciworldwide.exception.handler;

import com.aciworldwide.dto.GenericApiResponse;
import com.aciworldwide.exception.AppException;
import com.aciworldwide.exception.BusinessException;
import com.aciworldwide.exception.DatabaseException;
import com.aciworldwide.exception.ResourceNotFoundException;
import com.aciworldwide.exception.ServiceException;
import com.aciworldwide.exception.BusinessValidationException;
import com.aciworldwide.exception.mapper.ExceptionMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.BodyProcessorException;
import io.vertx.ext.web.validation.ParameterProcessorException;
import io.vertx.ext.web.validation.RequestPredicateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Global exception handler for Vert.x routing context.
 * Acts similar to Spring's @ControllerAdvice but for Vert.x failure handlers.
 */
@Slf4j
@Component
public class GlobalExceptionHandler {

    private final ExceptionMapper exceptionMapper;
    private final Map<Class<? extends Throwable>, ExceptionHandlerStrategy> handlerStrategies;

    public GlobalExceptionHandler(ExceptionMapper exceptionMapper) {
        this.exceptionMapper = exceptionMapper;
        this.handlerStrategies = initializeHandlerStrategies();
    }

    /**
     * Initialize exception handler strategies for clean exception handling.
     */
    private Map<Class<? extends Throwable>, ExceptionHandlerStrategy> initializeHandlerStrategies() {
        Map<Class<? extends Throwable>, ExceptionHandlerStrategy> strategies = new HashMap<>();
        
        // OpenAPI validation exceptions - simple generic handling
        strategies.put(BodyProcessorException.class, new ExceptionHandlerStrategy(
            this::handleOpenApiValidationException, 400));
        strategies.put(ParameterProcessorException.class, new ExceptionHandlerStrategy(
            this::handleOpenApiValidationException, 400));
        strategies.put(RequestPredicateException.class, new ExceptionHandlerStrategy(
            this::handleOpenApiValidationException, 400));
            
        // Application exceptions
        strategies.put(BusinessValidationException.class, new ExceptionHandlerStrategy(
            exc -> exceptionMapper.mapAppException((BusinessValidationException) exc), 400));
        strategies.put(BusinessException.class, new ExceptionHandlerStrategy(
            exc -> exceptionMapper.mapAppException((BusinessException) exc), 422));
        strategies.put(ResourceNotFoundException.class, new ExceptionHandlerStrategy(
            exc -> exceptionMapper.mapAppException((ResourceNotFoundException) exc), 404));
        strategies.put(DatabaseException.class, new ExceptionHandlerStrategy(
            exc -> exceptionMapper.mapAppException((DatabaseException) exc), 500));
        strategies.put(ServiceException.class, new ExceptionHandlerStrategy(
            exc -> exceptionMapper.mapAppException((ServiceException) exc), 
            exc -> ((ServiceException) exc).getHttpStatus()));
        strategies.put(AppException.class, new ExceptionHandlerStrategy(
            exc -> exceptionMapper.mapAppException((AppException) exc),
            exc -> ((AppException) exc).getHttpStatus()));
            
        // Generic exceptions
        strategies.put(RuntimeException.class, new ExceptionHandlerStrategy(
            exc -> exceptionMapper.mapRuntimeException((RuntimeException) exc), 500));
        strategies.put(Exception.class, new ExceptionHandlerStrategy(
            exc -> exceptionMapper.mapGenericException((Exception) exc), 500));
            
        return strategies;
    }

    /**
     * Strategy class for handling exceptions with response and status code.
     */
    private static class ExceptionHandlerStrategy {
        private final Function<Throwable, GenericApiResponse<Void>> responseMapper;
        private final Function<Throwable, Integer> statusCodeMapper;

        public ExceptionHandlerStrategy(Function<Throwable, GenericApiResponse<Void>> responseMapper, int statusCode) {
            this.responseMapper = responseMapper;
            this.statusCodeMapper = exc -> statusCode;
        }

        public ExceptionHandlerStrategy(Function<Throwable, GenericApiResponse<Void>> responseMapper, 
                                      Function<Throwable, Integer> statusCodeMapper) {
            this.responseMapper = responseMapper;
            this.statusCodeMapper = statusCodeMapper;
        }

        public GenericApiResponse<Void> handleException(Throwable exception) {
            return responseMapper.apply(exception);
        }

        public int getStatusCode(Throwable exception) {
            return statusCodeMapper.apply(exception);
        }
    }

    /**
     * Main failure handler method that handles all exceptions.
     */
    public void handleFailure(RoutingContext context) {
        Throwable failure = context.failure();
        
        if (failure == null) {
            handleUnknownFailure(context);
            return;
        }

        log.debug("Handling failure: {}", failure.getClass().getSimpleName());

        // Use strategy pattern for clean exception handling
        ExceptionHandlerStrategy strategy = findHandlerStrategy(failure);
        GenericApiResponse<Void> response = strategy.handleException(failure);
        int statusCode = strategy.getStatusCode(failure);

        sendErrorResponse(context, statusCode, response);
    }

    /**
     * Finds the appropriate handler strategy for the given exception.
     */
    private ExceptionHandlerStrategy findHandlerStrategy(Throwable exception) {
        // Check for exact match first
        ExceptionHandlerStrategy strategy = handlerStrategies.get(exception.getClass());
        if (strategy != null) {
            return strategy;
        }
        
        // Check for inheritance hierarchy
        for (Map.Entry<Class<? extends Throwable>, ExceptionHandlerStrategy> entry : handlerStrategies.entrySet()) {
            if (entry.getKey().isAssignableFrom(exception.getClass())) {
                return entry.getValue();
            }
        }
        
        // Fallback to generic exception handler
        return handlerStrategies.get(Exception.class);
    }

    /**
     * Handles OpenAPI validation exceptions with simple generic message.
     */
    private GenericApiResponse<Void> handleOpenApiValidationException(Throwable exception) {
        log.warn("OpenAPI validation failed: {}", exception.getMessage());
        return GenericApiResponse.error("Request validation failed. Please check your request body, parameters, and headers against the API specification.", 400);
    }

    /**
     * Handles unknown failures (when context.failure() is null).
     */
    private void handleUnknownFailure(RoutingContext context) {
        log.error("Unknown failure occurred - no exception available");
        GenericApiResponse<Void> response = GenericApiResponse.error("An unknown error occurred", 500);
        sendErrorResponse(context, 500, response);
    }

    /**
     * Sends the error response to the client.
     */
    private void sendErrorResponse(RoutingContext context, int statusCode, GenericApiResponse<Void> response) {
        if (context.response().ended()) {
            log.warn("Response already ended, cannot send error response");
            return;
        }

        try {
            context.response()
                    .setStatusCode(statusCode)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(response).encodePrettily());
        } catch (Exception e) {
            log.error("Failed to send error response", e);
            try {
                if (!context.response().ended()) {
                    context.response()
                            .setStatusCode(500)
                            .putHeader("Content-Type", "application/json")
                            .end("{\"status\":\"error\",\"message\":\"Internal server error\"}");
                }
            } catch (Exception fallbackException) {
                log.error("Failed to send fallback error response", fallbackException);
            }
        }
    }

    /**
     * Utility method to fail a routing context with an exception.
     */
    public static void failContext(RoutingContext context, Throwable exception) {
        context.fail(exception);
    }
}
