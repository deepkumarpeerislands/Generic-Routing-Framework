package com.aciworldwide.deployment;

import com.aciworldwide.verticle.HttpServerVerticle;
import com.aciworldwide.verticle.OpenAPIVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

/**
 * Simple service to deploy Vert.x verticles automatically on application startup.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VerticleDeploymentService {

    private final Vertx vertx;
    private final JsonObject serverConfig;
    private final OpenAPIVerticle openAPIVerticle;
    private final HttpServerVerticle httpServerVerticle;

    private String openApiDeploymentId;
    private String httpServerDeploymentId;

    @EventListener(ApplicationReadyEvent.class)
    public void deployVerticles() {
        log.info("Auto-deploying verticles...");
        
        DeploymentOptions options = new DeploymentOptions().setConfig(serverConfig);
        
        // Deploy OpenAPI Verticle first
        vertx.deployVerticle(openAPIVerticle, options)
            .onSuccess(deploymentId -> {
                this.openApiDeploymentId = deploymentId;
                log.info("OpenAPI Verticle deployed: {}", deploymentId);

                vertx.deployVerticle(httpServerVerticle, options)
                    .onSuccess(httpDeploymentId -> {
                        this.httpServerDeploymentId = httpDeploymentId;
                        log.info("HTTP Server Verticle deployed: {}", httpDeploymentId);
                        log.info("Application ready - HTTP server listening");
                    })
                    .onFailure(throwable -> {
                        log.error("Failed to deploy HTTP Server Verticle", throwable);
                    });
            })
            .onFailure(throwable -> {
                log.error("Failed to deploy OpenAPI Verticle", throwable);
            });
    }

    @PreDestroy
    public void undeployVerticles() {
        log.info("Shutting down verticles...");
        
        if (httpServerDeploymentId != null) {
            vertx.undeploy(httpServerDeploymentId)
                .onSuccess(v -> log.info("HTTP Server Verticle stopped"))
                .onFailure(throwable -> log.error("Error stopping HTTP Server Verticle", throwable));
        }
        
        if (openApiDeploymentId != null) {
            vertx.undeploy(openApiDeploymentId)
                .onSuccess(v -> log.info("OpenAPI Verticle stopped"))
                .onFailure(throwable -> log.error("Error stopping OpenAPI Verticle", throwable));
        }
        
        vertx.close()
            .onSuccess(v -> log.info("Vert.x instance closed"))
            .onFailure(throwable -> log.error("Error closing Vert.x", throwable));
    }
}
