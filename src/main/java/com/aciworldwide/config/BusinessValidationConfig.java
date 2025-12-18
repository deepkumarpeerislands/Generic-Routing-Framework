package com.aciworldwide.config;

import com.aciworldwide.validation.RequestValidator;
import com.aciworldwide.validation.business.BusinessValidationService;
import com.aciworldwide.validation.business.ClasspathValidationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for business validation.
 * 
 * Can be controlled via application.yml:
 *   validation.business.enabled: true/false (default: true)
 *   validation.business.base-path: path to validation files (default: validations)
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "validation.business.enabled", havingValue = "true", matchIfMissing = true)
public class BusinessValidationConfig {

    @Value("${validation.business.base-path:validation/business}")
    private String basePath;

    @Bean
    public ClasspathValidationRepository businessValidationRepository() {
        return new ClasspathValidationRepository(basePath);
    }

    @Bean
    public RequestValidator validator(ClasspathValidationRepository repository) {
        return new BusinessValidationService(repository);
    }
}

