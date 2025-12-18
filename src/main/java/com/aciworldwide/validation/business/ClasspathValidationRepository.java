package com.aciworldwide.validation.business;

import com.aciworldwide.validation.business.config.ValidationConfig;
import com.aciworldwide.validation.business.config.ValidationDefinition;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Loads business validation definitions per entity from classpath:
 *   {basePath}/{entity}.yml
 *
 * Uses Spring @Cacheable and @CircuitBreaker for resilience.
 */
@Slf4j
public class ClasspathValidationRepository {

    private final String basePath;

    public ClasspathValidationRepository(String basePath) {
        this.basePath = basePath;
    }

    public List<ValidationDefinition> find(String entity, String operationId) {
        List<ValidationDefinition> allForEntity = loadForEntity(entity);
        return allForEntity.stream()
                .filter(v -> v.getOperationId().equals(operationId))
                .toList();
    }

    /**
     * Loads validation definitions with caching and circuit breaker protection.
     * 
     * @Cacheable - caches result per entity (lazy-load once)
     * @CircuitBreaker - protects against repeated failures
     */
    @Cacheable(value = "validationDefinitions", key = "#entity")
    @CircuitBreaker(name = "validationRepository", fallbackMethod = "loadForEntityFallback")
    private List<ValidationDefinition> loadForEntity(String entity) {
        String path = basePath + "/" + entity + ".yml";
        Resource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            log.debug("No business validation file for entity '{}' at '{}'", entity, path);
            return List.of();
        }

        try (InputStream is = resource.getInputStream()) {
            LoaderOptions options = new LoaderOptions();
            Constructor constructor = new Constructor(ValidationConfig.class, options);
            Yaml yaml = new Yaml(constructor);
            ValidationConfig doc = yaml.load(is);
            List<ValidationDefinition> list = Optional.ofNullable(doc.getRules()).orElse(List.of());
            log.info("Loaded {} business validation definition(s) for entity '{}' from '{}'", list.size(), entity, path);
            return list;
        } catch (Exception e) {
            log.error("Failed to load business validations for entity '{}' from '{}': {}", entity, path, e.getMessage());
            throw new RuntimeException("Failed to load validation file: " + path, e);
        }
    }
    
    /**
     * Fallback method for circuit breaker - returns empty list when circuit is open.
     */
    private List<ValidationDefinition> loadForEntityFallback(String entity, Exception e) {
        log.warn("Circuit breaker OPEN for entity '{}' - using fallback (empty list). Reason: {}", 
                entity, e.getMessage());
        return List.of();
    }
}

