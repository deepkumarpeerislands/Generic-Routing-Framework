package com.aciworldwide.config;

import com.aciworldwide.handler.GenericCrudHandler;
import com.aciworldwide.verticle.HttpServerVerticle;
import com.aciworldwide.verticle.OpenAPIVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class VertxConfig {

    @Value("${server.host:0.0.0.0}")
    private String serverHost;

    @Value("${server.port:8080}")
    private Integer serverPort;

    @Value("${server.timeout:30000}")
    private Long serverTimeout;

    @Value("${openapi.spec.path:openapi/api-spec.yaml}")
    private String openApiSpecPath;

    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public JsonObject serverConfig() {
        return new JsonObject()
                .put("server.host", serverHost)
                .put("server.port", serverPort)
                .put("server.timeout", serverTimeout)
                .put("openapi.spec.path", openApiSpecPath);
    }

    @Bean
    public HttpServerVerticle httpServerVerticle(JsonObject serverConfig) {
        HttpServerVerticle verticle = new HttpServerVerticle();
        // Set configuration for the verticle
        return verticle;
    }

    @Bean
    public GenericCrudHandler genericHandler() {
        return new GenericCrudHandler();
    }

    @Bean
    public OpenAPIVerticle openAPIVerticle(GenericCrudHandler genericCrudHandler) {
        return new OpenAPIVerticle(genericCrudHandler);
    }
}
