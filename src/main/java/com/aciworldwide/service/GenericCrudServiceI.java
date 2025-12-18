package com.aciworldwide.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * Generic CRUD service interface for all entities.
 */
public interface GenericCrudServiceI {

    /**
     * Creates a new entity.
     */
    Future<JsonObject> create(String entity, JsonObject data);

    /**
     * Retrieves an entity by ID.
     */
    Future<JsonObject> getById(String entity, String entityId);

    /**
     * Updates an entity.
     */
    Future<JsonObject> update(String entity, String entityId, JsonObject updateData);

    /**
     * Deletes an entity.
     */
    Future<Void> delete(String entity, String entityId);

    /**
     * Lists entities with optional filtering.
     */
    Future<JsonObject> list(String entity, JsonObject filters);
}
