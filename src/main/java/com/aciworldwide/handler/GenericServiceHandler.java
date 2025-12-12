package com.aciworldwide.handler;

import com.aciworldwide.dto.GenericApiResponse;
import com.aciworldwide.validation.RequestValidator;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Generic service handler for business logic pipeline.
 * 
 * Responsibilities:
 * - Business validation & enrichment
 * - Service/repository calls
 * - Business result mapping
 */
@Slf4j
@Component
public class GenericServiceHandler {

    private final RequestValidator validator;

    public GenericServiceHandler(@Autowired(required = false) RequestValidator validator) {
        this.validator = validator;
    }

    /**
     * Main business pipeline: validation → enrichment → service call.
     *
     * @return GenericApiResponse with result or validation errors
     */
    public GenericApiResponse<?> handle(String entity, String operationId, String action, JsonObject payload) {
        log.debug("Service handler processing: entity={}, operationId={}, action={}", entity, operationId, action);

        // 1. Business validation & enrichment (mutates payload if actions exist)
        Map<String, String> validationErrors = applyBusinessValidation(entity, operationId, payload);
        if (!validationErrors.isEmpty()) {
            return GenericApiResponse.error("Business validation failed", validationErrors);
        }

        // 2. Execute service logic based on action
        return switch (action) {
            case "create" -> createEntity(entity, payload);
            case "getById" -> getEntityById(entity, payload);
            default -> GenericApiResponse.error("Operation '" + operationId + "' is not implemented");
        };
    }

    private Map<String, String> applyBusinessValidation(String entity, String operationId, JsonObject payload) {
        if (validator == null) {
            return Map.of();
        }
        return validator.validate(entity, operationId, payload);
    }

    private GenericApiResponse<JsonObject> createEntity(String entity, JsonObject payload) {
        log.info("Creating entity [{}]", entity);
        log.debug("Payload after enrichment: {}", payload);

        JsonObject createdEntity = new JsonObject()
                .put("id", UUID.randomUUID().toString())
                .put("entity", entity)
                .put("payload", payload)
                .put("createdAt", Instant.now().toString());

        return GenericApiResponse.success("Entity created successfully", createdEntity);
    }

    private GenericApiResponse<JsonObject> getEntityById(String entity, JsonObject payload) {
        String entityId = payload.getString("entityId");
        log.info("Retrieving entity [{}] with id [{}]", entity, entityId);

        JsonObject fetched = new JsonObject()
                .put("id", entityId)
                .put("entity", entity)
                .put("payload", new JsonObject().put("status", "dummy"))
                .put("retrievedAt", Instant.now().toString());

        return GenericApiResponse.success("Entity retrieved successfully", fetched);
    }
}

