package com.aciworldwide.verticle;

import com.aciworldwide.handler.GenericRoutingHandler;
import com.aciworldwide.registry.RouterRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.openapi.RouterBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAPI Verticle responsible for loading and configuring OpenAPI specifications.
 * This verticle handles OpenAPI schema validation and route generation.
 */
@Slf4j
public class OpenAPIVerticle extends AbstractVerticle {

    public static final String ROUTER_READY_ADDRESS = "router.openapi.ready";
    
    private RouterBuilder routerBuilder;
    private final GenericRoutingHandler genericRoutingHandler;
    
    public OpenAPIVerticle(GenericRoutingHandler genericRoutingHandler) {
        this.genericRoutingHandler = genericRoutingHandler;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        log.info("Starting OpenAPI Verticle...");
        
        try {
            String specPath = getOpenAPISpecPath();
            
            // Load OpenAPI specification
            RouterBuilder.create(vertx, specPath)
                .onSuccess(builder -> {
                    this.routerBuilder = builder;
                    configureRouterBuilder();
                    configureOperationHandlers();
                    createAndStoreRouter();
                    
                    log.info("OpenAPI Verticle started successfully with spec: {}", specPath);
                    startPromise.complete();
                })
                .onFailure(throwable -> {
                    log.error("Failed to load OpenAPI specification from: {}", specPath, throwable);
                    startPromise.fail(throwable);
                });
                
        } catch (Exception e) {
            log.error("Error during OpenAPI Verticle startup", e);
            startPromise.fail(e);
        }
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        log.info("Stopping OpenAPI Verticle...");
        
        if (routerBuilder != null) {
            // Clean up resources if needed
            routerBuilder = null;
        }
        
        log.info("OpenAPI Verticle stopped successfully");
        stopPromise.complete();
    }

    /**
     * Configures the RouterBuilder with handlers.
     */
    private void configureRouterBuilder() {
        if (routerBuilder == null) {
            log.warn("RouterBuilder is null, cannot configure");
            return;
        }
        
        // Add body handler first to parse JSON request bodies
        routerBuilder.rootHandler(io.vertx.ext.web.handler.BodyHandler.create());
        
        // Configure operation handlers - this will be called from service
        
        log.debug("RouterBuilder configured successfully");
    }

    /**
     * Creates a router from the configured RouterBuilder and stores it in registry.
     */
    public void createAndStoreRouter() {
        if (routerBuilder == null) {
            log.error("RouterBuilder is not initialized");
            return;
        }
        
        try {
            // Build router
            Router router = routerBuilder.createRouter();
            
            // Store router in registry (in-process) for direct retrieval by other verticles
            RouterRegistry.setRouter(router);
            
            // Publish readiness on event bus (service-style)
            JsonObject payload = new JsonObject().put("status", "ready");
            vertx.eventBus().publish(ROUTER_READY_ADDRESS, payload, new DeliveryOptions());
            
            log.info("OpenAPI router created and published readiness on '{}'", ROUTER_READY_ADDRESS);
        } catch (Exception e) {
            log.error("Failed to create router from OpenAPI spec", e);
        }
    }

    /**
     * Gets the OpenAPI specification path from configuration.
     *
     * @return OpenAPI spec path
     */
    private String getOpenAPISpecPath() {
        return config().getString("openapi.spec.path");
    }

    private void configureOperationHandlers() {
        if (routerBuilder != null && genericRoutingHandler != null) {
            routerBuilder.operations().forEach(operation -> {
                String operationId = operation.getOperationId();
                operation.handler(ctx -> genericRoutingHandler.handle(operationId, ctx));
            });
            log.debug("Operation handlers configured");
        }
    }

    /**
     * Gets the RouterBuilder instance.
     *
     * @return RouterBuilder instance
     */
    public RouterBuilder getRouterBuilder() {
        return routerBuilder;
    }
}
