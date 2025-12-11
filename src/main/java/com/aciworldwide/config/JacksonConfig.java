package com.aciworldwide.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.vertx.core.json.jackson.DatabindCodec;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Jackson configuration to support JDK8 features like Optional in Vert.x.
 */
@Configuration
public class JacksonConfig {

    @PostConstruct
    public void configureVertxJackson() {
        // Configure Vert.x's Jackson ObjectMapper to support JDK8 Optional
        ObjectMapper mapper = DatabindCodec.mapper();
        mapper.registerModule(new Jdk8Module());
    }
}
