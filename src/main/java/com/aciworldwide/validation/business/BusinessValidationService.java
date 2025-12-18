package com.aciworldwide.validation.business;

import com.aciworldwide.validation.RequestValidator;
import com.aciworldwide.validation.business.config.ValidationDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Business validation + enrichment implementation.
 *
 * Processing order per definition:
 * 1) Execute enrichment actions (if present)
 * 2) Evaluate validation condition (if present) and collect violations
 *
 * Convention: if validation condition evaluates to TRUE => violation.
 */
@Slf4j
public class BusinessValidationService implements RequestValidator {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ClasspathValidationRepository repository;
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public BusinessValidationService(ClasspathValidationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Map<String, String> validate(String entity, String operationId, Object payload) {
        List<ValidationDefinition> defs = repository.find(entity, operationId);
        if (defs.isEmpty()) {
            return Map.of();
        }

    /*
    * Creates a SpEL context where expressions can be evaluated
    * Registers the payload as variable #payload (accessible in SpEL expressions) */

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("payload", payload);

        Map<String, String> violations = new LinkedHashMap<>();
        for (ValidationDefinition def : defs) {
            try {
                executeActions(def, context);
                evaluateValidation(def, context, violations);
            } catch (RuntimeException e) {
                log.warn("Error processing validation '{}' for entity '{}' op '{}': {}",
                        def.getId(), entity, operationId, e.getMessage());
                violations.put(def.getId(), "Rule processing failed: " + e.getMessage());
            }
        }

        return violations;
    }

    private void executeActions(ValidationDefinition def, StandardEvaluationContext context) {
        if (!def.hasActions()) {
            return;
        }
        for (String expr : def.getActions()) {
            if (expr == null || expr.isBlank()) {
                continue;
            }

            /*
            * Parses the SpEL expression string into an executable Expression object
            * Executes the expression using the context (which has #payload variable)*/
            getParsedExpression(expr).getValue(context);
        }
    }

    private void evaluateValidation(ValidationDefinition def,
                                    StandardEvaluationContext context,
                                    Map<String, String> violations) {
        if (!def.hasValidation()) {
            return;
        }
        Expression condition = getParsedExpression(def.getCondition());
        Boolean violated = condition.getValue(context, Boolean.class);
        if (Boolean.TRUE.equals(violated)) {
            violations.put(def.getId(), def.getMessage());
        }
    }

    /**
     * Gets a parsed SpEL expression from cache, or parses and caches it on first use.
     * Thread-safe via ConcurrentHashMap.
     *
     * @param expr the SpEL expression string
     * @return compiled Expression object
     */
    private Expression getParsedExpression(String expr) {
        return expressionCache.computeIfAbsent(expr, parser::parseExpression);
    }
}

