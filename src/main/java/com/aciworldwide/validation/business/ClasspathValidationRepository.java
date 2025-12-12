package com.aciworldwide.validation.business;

import com.aciworldwide.validation.business.config.ValidationConfig;
import com.aciworldwide.validation.business.config.ValidationDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads business validation definitions per entity from classpath:
 *   {basePath}/{entity}.yml
 *
 * Lazy-loads on first request per entity and caches results.
 */
@Slf4j
public class ClasspathValidationRepository {

    private final Map<String, List<ValidationDefinition>> cache = new ConcurrentHashMap<>();
    private final String basePath;

    public ClasspathValidationRepository(String basePath) {
        this.basePath = basePath;
    }

    public List<ValidationDefinition> find(String entity, String operationId) {
        List<ValidationDefinition> allForEntity = cache.computeIfAbsent(entity, this::loadForEntity);
        return allForEntity.stream()
                .filter(v -> v.getOperationId().equals(operationId))
                .toList();
    }

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
        } catch (IOException e) {
            log.warn("Failed to load business validations for entity '{}' from '{}': {}", entity, path, e.getMessage());
            return List.of();
        }
    }
}

