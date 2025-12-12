package com.aciworldwide.validation;

import java.util.Map;

/**
 * Framework-facing validation contract for request payload validation/enrichment.
 * Implementations can use any strategy (SpEL, Drools, scripts, remote service, etc.).
 */
public interface RequestValidator {

    /**
     * Validate and/or enrich the payload for a given entity and operation.
     *
     * @param entity the entity type from the request path
     * @param operationId the OpenAPI operation identifier
     * @param payload the request payload (typically JsonObject or DTO)
     * @return map of violationId -> message; empty map means "no violations"
     */
    Map<String, String> validate(String entity, String operationId, Object payload);
}

