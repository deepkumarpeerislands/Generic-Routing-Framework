package com.aciworldwide.validation.business.config;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * A single business validation/enrichment definition, selected by (entity, operationId).
 * Mutable data class with builder pattern for YAML deserialization compatibility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationDefinition {

    private String id;
    private String entity;
    private String operationId;
    private String condition;
    private String message;
    private List<String> actions;

    public boolean hasValidation() {
        return condition != null && !condition.isBlank()
                && message != null && !message.isBlank();
    }

    public boolean hasActions() {
        return actions != null && !actions.isEmpty();
    }
}

