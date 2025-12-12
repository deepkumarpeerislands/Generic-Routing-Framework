package com.aciworldwide.handler;

import com.aciworldwide.dto.GenericApiResponse;
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
        if (operationId == null || operationId.isBlank()) {
            respond(context, 400, GenericApiResponse.error("Missing operationId"));
            return;
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
        GenericApiResponse<?> result = serviceHandler.handle(entity, operationId, action, payload);

        // Map to HTTP response
        int statusCode = mapStatusCode(result, action);
        respond(context, statusCode, result);
    }

    /**
     * Builds payload from request body and path parameters.
     */
    private JsonObject buildPayload(RoutingContext context) {
        JsonObject payload = safeBody(context);
        
        // Add path params to payload for service layer
        String entityId = context.pathParam("entityId");
        if (entityId != null) {
            payload.put("entityId", entityId);
        }
        
        return payload;
    }

    /**
     * Maps business result to HTTP status code.
     */
    private int mapStatusCode(GenericApiResponse<?> result, String action) {
        if ("error".equals(result.getStatus())) {
            return 400; // Validation/business errors
        }
        return "create".equals(action) ? 201 : 200;
    }

    private JsonObject safeBody(RoutingContext context) {
        if (context.body() == null || context.body().buffer() == null) {
            return new JsonObject();
        }
        JsonObject json = context.body().asJsonObject();
        return json == null ? new JsonObject() : json;
    }

    private void respond(RoutingContext context, int status, GenericApiResponse<?> response) {
        context.response()
                .setStatusCode(status)
                .putHeader("Content-Type", "application/json")
                .end(JsonObject.mapFrom(response).encodePrettily());
    }
}

