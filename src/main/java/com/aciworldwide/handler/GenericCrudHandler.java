package com.aciworldwide.handler;

import com.aciworldwide.dto.GenericApiResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

/**
 * Generic handler for entity CRUD operations.
 */
@Slf4j
public class GenericCrudHandler {

    /**
     * Dispatches operations based on the operationId conventions.
     *
     * @param operationId operationId defined in the OpenAPI spec
     * @param context     the routing context
     */
    public void handle(String operationId, RoutingContext context) {
        if (operationId == null || operationId.isBlank()) {
            fail(context, 400, "Missing operationId");
            return;
        }

        String entity = context.pathParam("entity");
        if (entity == null || entity.isBlank()) {
            entity = operationId.contains(".") ? operationId.substring(0, operationId.indexOf('.')) : operationId;
        }

        String action = operationId.contains(".") ? operationId.substring(operationId.indexOf('.') + 1) : operationId;

        switch (action) {
            case "create" -> createEntity(entity, context);
            case "getById" -> getEntityById(entity, context);
            default -> fail(context, 501, "Operation '" + operationId + "' is not implemented");
        }
    }

    private void createEntity(String entity, RoutingContext context) {
        log.info("Creating entity [{}]", entity);
        JsonObject requestBody = context.body().asJsonObject();
        log.debug("Request body: {}", requestBody);

        JsonObject createdEntity = new JsonObject()
            .put("id", UUID.randomUUID().toString())
            .put("entity", entity)
            .put("payload", requestBody)
            .put("createdAt", Instant.now().toString());

        GenericApiResponse<JsonObject> response = GenericApiResponse.success("Entity created successfully", createdEntity);
        respond(context, 201, response);
    }

    private void getEntityById(String entity, RoutingContext context) {
        String entityId = context.pathParam("entityId");
        log.info("Retrieving entity [{}] with id [{}]", entity, entityId);

        JsonObject fetched = new JsonObject()
            .put("id", entityId)
            .put("entity", entity)
            .put("payload", new JsonObject().put("status", "dummy"))
            .put("retrievedAt", Instant.now().toString());

        GenericApiResponse<JsonObject> response = GenericApiResponse.success("Entity retrieved successfully", fetched);
        respond(context, 200, response);
    }

    private void fail(RoutingContext context, int status, String message) {
        GenericApiResponse<Object> response = GenericApiResponse.error(message);
        respond(context, status, response);
    }

    private void respond(RoutingContext context, int status, GenericApiResponse<?> response) {
        context.response()
            .setStatusCode(status)
            .putHeader("Content-Type", "application/json")
            .end(JsonObject.mapFrom(response).encodePrettily());
    }
}
