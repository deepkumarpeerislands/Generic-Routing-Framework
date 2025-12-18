package com.aciworldwide.handler;

import com.aciworldwide.dto.GenericApiResponse;
import com.aciworldwide.exception.BusinessException;
import com.aciworldwide.exception.BusinessValidationException;
import com.aciworldwide.exception.ResourceNotFoundException;
import com.aciworldwide.service.GenericCrudServiceI;
import com.aciworldwide.validation.RequestValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    private final GenericCrudServiceI crudService;

    public GenericServiceHandler(@Autowired(required = false) RequestValidator validator,
                                GenericCrudServiceI crudService) {
        this.validator = validator;
        this.crudService = crudService;
    }

    /**
     * Main business pipeline: validation → enrichment → service call.
     *
     * @return Future containing GenericApiResponse with result or failure
     */
    public Future<GenericApiResponse<?>> executeAction(String entity, String operationId, String action, JsonObject payload) {
        log.debug("Service handler processing: entity={}, operationId={}, action={}", entity, operationId, action);

        return Future.future(promise -> {
            try {
                // 1. Business validation & enrichment (throws ValidationException if validation fails)
                applyBusinessValidation(entity, operationId, payload);

                // 2. Execute service logic based on action using Future composition
                Future<GenericApiResponse<?>> actionFuture = switch (action) {
                    case "create" -> executeCreateAction(entity, payload);
                    case "getById" -> executeGetByIdAction(entity, payload);
                    case "update" -> executeUpdateAction(entity, payload);
                    case "delete" -> executeDeleteAction(entity, payload);
                    case "list" -> executeListAction(entity, payload);
                    default -> Future.failedFuture(new BusinessException("Operation '" + operationId + "' is not implemented"));
                };
                
                actionFuture
                    .onSuccess(promise::complete)
                    .onFailure(promise::fail);
                    
            } catch (Exception e) {
                promise.fail(e);
            }
        });
    }

    private void applyBusinessValidation(String entity, String operationId, JsonObject payload) {
        if (validator == null) {
            return;
        }
        Map<String, String> validationErrors = validator.validate(entity, operationId, payload);

        // If there are validation errors, throw ValidationException instead of returning them
        if (!validationErrors.isEmpty()) {
            log.info("Business Validation Failed : "+validationErrors);
            throw new BusinessValidationException("Business validation failed", validationErrors);
        }

    }

    // Future-based action execution methods following Vert.x patterns
    
    private Future<GenericApiResponse<?>> executeCreateAction(String entity, JsonObject payload) {
        log.info("Creating entity [{}]", entity);
        log.debug("Payload after enrichment: {}", payload);
        
        return crudService.create(entity, payload)
            .map(createdEntity -> GenericApiResponse.success("Entity created successfully", createdEntity, 201));
    }

    private Future<GenericApiResponse<?>> executeGetByIdAction(String entity, JsonObject payload) {
        // Extract entityId from path parameters
        String entityId = extractEntityId(payload);
        
        log.info("Retrieving entity [{}] with id [{}]", entity, entityId);

        return crudService.getById(entity, entityId)
            .map(fetchedEntity -> GenericApiResponse.success("Entity retrieved successfully", fetchedEntity, 200));
    }
    
    private Future<GenericApiResponse<?>> executeUpdateAction(String entity, JsonObject payload) {
        String entityId = extractEntityId(payload);
        
        log.info("Updating entity [{}] with id [{}]", entity, entityId);
        
        return crudService.update(entity, entityId, payload)
            .map(updatedEntity -> GenericApiResponse.success("Entity updated successfully", updatedEntity, 200));
    }
    
    private Future<GenericApiResponse<?>> executeDeleteAction(String entity, JsonObject payload) {
        String entityId = extractEntityId(payload);
        
        log.info("Deleting entity [{}] with id [{}]", entity, entityId);
        
        return crudService.delete(entity, entityId)
            .map(v -> GenericApiResponse.success("Entity deleted successfully", 204));
    }
    
    private Future<GenericApiResponse<?>> executeListAction(String entity, JsonObject payload) {
        log.info("Listing entities [{}]", entity);
        
        // Extract filters from query parameters
        JsonObject filters = payload.getJsonObject("_query", new JsonObject());
        
        return crudService.list(entity, filters)
            .map(listResult -> GenericApiResponse.success("Entities retrieved successfully", listResult, 200));
    }
    
    private String extractEntityId(JsonObject payload) {
        JsonObject pathParams = payload.getJsonObject("_path");
        if (pathParams != null) {
            String entityId = pathParams.getString("entityId");
            if (entityId != null && !entityId.trim().isEmpty()) {
                return entityId;
            }
        }
        throw new BusinessValidationException("Entity ID is required in path parameters");
    }
}

