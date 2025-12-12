package com.aciworldwide.validation.business.config;

import java.util.List;

/**
 * A single business validation/enrichment definition, selected by (entity, operationId).
 */
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }
}

