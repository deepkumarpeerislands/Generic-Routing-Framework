package com.aciworldwide.handler;

import com.aciworldwide.dto.GenericApiResponse;
import com.aciworldwide.exception.BusinessValidationException;
import com.aciworldwide.exception.handler.GlobalExceptionHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Thin HTTP routing layer handler.
 * 
 * Responsibilities:
 * - Extract request data (entity, operationId, payload, path params)
 * - Delegate to service layer
 * - Map service results to HTTP responses
 */
@Slf4j
@Component
public class GenericRoutingHandler {

    private final GenericServiceHandler serviceHandler;

    public GenericRoutingHandler(GenericServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    public void handle(String operationId, RoutingContext context) {
        try {
            if (operationId == null || operationId.isBlank()) {
                throw new BusinessValidationException("Missing operationId");
            }

            // Extract entity from path or operationId
            String entity = context.pathParam("entity");
            if (entity == null || entity.isBlank()) {
                entity = operationId.contains(".") ? operationId.substring(0, operationId.indexOf('.')) : operationId;
            }

            // Extract action from operationId
            String action = operationId.contains(".") ? operationId.substring(operationId.indexOf('.') + 1) : operationId;

            // Extract and enrich payload with path params
            JsonObject payload = buildPayload(context);

            // Delegate to service layer (validation + business logic)
            // Using Future-based approach following Vert.x patterns
            serviceHandler.executeAction(entity, operationId, action, payload)
                .onSuccess(result -> {
                    // Use status code from response
                    respond(context, result.getStatusCode(), result);
                })
                .onFailure(throwable -> {
                    // Service layer exceptions propagated to global handler
                    log.debug("Service layer exception, propagating to global handler: {}", throwable.getMessage());
                    GlobalExceptionHandler.failContext(context, throwable);
                });
            
        } catch (Exception e) {
            // Propagate all exceptions to the global exception handler
            log.debug("Exception in routing handler, propagating to global handler: {}", e.getMessage());
            GlobalExceptionHandler.failContext(context, e);
        }
    }

    /**
     * Builds payload from request body and enriches with path params, query params, and headers.
     * Structure:
     * {
     *   "bodyField": "value",           // Original body fields at top level
     *   "_path": { "entity": "users", "entityId": "123" },
     *   "_query": { "limit": "10", "status": ["ACTIVE"] },
     *   "_headers": { "x-request-id": "abc-123" }
     * }
     */
    private JsonObject buildPayload(RoutingContext context) {
        JsonObject payload = safeBody(context);
        
        // Add path parameters under _path
        JsonObject pathParams = new JsonObject();
        context.pathParams().forEach(pathParams::put);
        if (!pathParams.isEmpty()) {
            payload.put("_path", pathParams);
        }
        
        // Add query parameters under _query
        JsonObject queryParams = new JsonObject();
        context.queryParams().forEach((key, value) -> {
            if (context.queryParams().getAll(key).size() > 1) {
                // Multiple values for same key -> array
                queryParams.put(key, context.queryParams().getAll(key));
            } else {
                // Single value -> string
                queryParams.put(key, value);
            }
        });
        if (!queryParams.isEmpty()) {
            payload.put("_query", queryParams);
        }
        
        // Add selected headers under _headers (common ones for validation/business logic)
        JsonObject headers = new JsonObject();
        addHeaderIfPresent(context, headers, "x-request-id");
        addHeaderIfPresent(context, headers, "x-user-id");
        addHeaderIfPresent(context, headers, "authorization");
        addHeaderIfPresent(context, headers, "content-type");
        if (!headers.isEmpty()) {
            payload.put("_headers", headers);
        }
        
        return payload;
    }
    
    /**
     * Helper to add header to headers object if present.
     */
    private void addHeaderIfPresent(RoutingContext context, JsonObject headers, String headerName) {
        String value = context.request().getHeader(headerName);
        if (value != null && !value.isBlank()) {
            headers.put(headerName, value);
        }
    }


    private JsonObject safeBody(RoutingContext context) {
        if (context.body() == null || context.body().buffer() == null) {
            return new JsonObject();
        }
        
        try {
            JsonObject json = context.body().asJsonObject();
            return json == null ? new JsonObject() : json;
        } catch (Exception e) {
            throw new BusinessValidationException("Invalid JSON in request body", e);
        }
    }

    private void respond(RoutingContext context, int status, GenericApiResponse<?> response) {
        context.response()
                .setStatusCode(status)
                .putHeader("Content-Type", "application/json")
                .end(JsonObject.mapFrom(response).encodePrettily());
    }
}

