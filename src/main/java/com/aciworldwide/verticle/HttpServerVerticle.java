package com.aciworldwide.verticle;

import com.aciworldwide.registry.RouterRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP Server Verticle responsible for starting and configuring the HTTP server.
 * This verticle handles the web server lifecycle and routing configuration.
 */
@Slf4j
public class HttpServerVerticle extends AbstractVerticle {

    
    private HttpServer httpServer;

    @Override
    public void start(Promise<Void> startPromise) {
        log.info("Starting HTTP Server Verticle...");
        
        // Check if router is already available (OpenAPI Verticle deployed first)
        Router router = RouterRegistry.getRouter();
        if (router != null) {
            log.info("Router already available, starting HTTP server immediately");
            startHttpServer(startPromise);
        } else {
            // Listen for router readiness if not available yet
            vertx.eventBus().consumer(OpenAPIVerticle.ROUTER_READY_ADDRESS, message -> {
                log.info("Received router ready notification, starting HTTP server");
                startHttpServer(startPromise);
            });
        }
    }

    private void startHttpServer(Promise<Void> startPromise) {
        try {
            // Get the router from registry (created by OpenAPIVerticle)
            Router router = RouterRegistry.getRouter();
            if (router == null) {
                log.error("Router not available in registry");
                startPromise.fail("Router not available");
                return;
            }
            
            // Configure HTTP server options
            HttpServerOptions serverOptions = createServerOptions();
            
            // Create HTTP server
            httpServer = vertx.createHttpServer(serverOptions);
            
            // Start the server with the OpenAPI router directly
            httpServer
                .requestHandler(router)
                .listen()
                .onSuccess(server -> {
                    int actualPort = server.actualPort();
                    String host = getHost();
                    log.info("HTTP Server started successfully on {}:{}", host, actualPort);
                    startPromise.complete();
                })
                .onFailure(throwable -> {
                    log.error("Failed to start HTTP Server", throwable);
                    startPromise.fail(throwable);
                });
                
        } catch (Exception e) {
            log.error("Error during HTTP Server startup", e);
            startPromise.fail(e);
        }
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        log.info("Stopping HTTP Server Verticle...");
        
        if (httpServer != null) {
            httpServer
                .close()
                .onSuccess(v -> {
                    log.info("HTTP Server stopped successfully");
                    stopPromise.complete();
                })
                .onFailure(throwable -> {
                    log.error("Error stopping HTTP Server", throwable);
                    stopPromise.fail(throwable);
                });
        } else {
            stopPromise.complete();
        }
    }


    /**
     * Creates HTTP server options.
     *
     * @return configured HttpServerOptions
     */
    private HttpServerOptions createServerOptions() {
        return new HttpServerOptions()
            .setHost(getHost())
            .setPort(getPort());
    }

    /**
     * Gets the server host from configuration.
     *
     * @return server host
     */
    private String getHost() {
        return config().getString("server.host");
    }

    /**
     * Gets the server port from configuration.
     *
     * @return server port
     */
    private int getPort() {
        return config().getInteger("server.port");
    }

}
