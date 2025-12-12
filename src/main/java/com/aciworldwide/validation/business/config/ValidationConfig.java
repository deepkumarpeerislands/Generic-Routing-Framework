package com.aciworldwide.validation.business.config;

import java.util.List;

/**
 * YAML root object. Kept in an internal package as it's a binding detail.
 */
public class ValidationConfig {

    // keep YAML key as "rules:" for backward compatibility with existing files
    private List<ValidationDefinition> rules;

    public List<ValidationDefinition> getRules() {
        return rules;
    }

    public void setRules(List<ValidationDefinition> rules) {
        this.rules = rules;
    }
}

