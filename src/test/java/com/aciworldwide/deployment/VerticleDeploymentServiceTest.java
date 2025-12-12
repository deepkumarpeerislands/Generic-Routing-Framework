package com.aciworldwide.deployment;

import com.aciworldwide.verticle.HttpServerVerticle;
import com.aciworldwide.verticle.OpenAPIVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VerticleDeploymentServiceTest {

    private Vertx vertx;
    private OpenAPIVerticle openAPIVerticle;
    private HttpServerVerticle httpServerVerticle;
    private VerticleDeploymentService service;

    @BeforeEach
    void setUp() {
        vertx = mock(Vertx.class);
        openAPIVerticle = mock(OpenAPIVerticle.class);
        httpServerVerticle = mock(HttpServerVerticle.class);
        JsonObject config = new JsonObject().put("server.host", "localhost");
        service = new VerticleDeploymentService(vertx, config, openAPIVerticle, httpServerVerticle);
    }

    @Test
    void deploysVerticlesInOrder() {
        when(vertx.deployVerticle(eq(openAPIVerticle), any(DeploymentOptions.class)))
                .thenReturn(Future.succeededFuture("openapi"));
        when(vertx.deployVerticle(eq(httpServerVerticle), any(DeploymentOptions.class)))
                .thenReturn(Future.succeededFuture("http"));

        service.deployVerticles();

        verify(vertx).deployVerticle(eq(openAPIVerticle), any(DeploymentOptions.class));
        verify(vertx).deployVerticle(eq(httpServerVerticle), any(DeploymentOptions.class));
    }

    @Test
    void handlesDeploymentFailure() {
        when(vertx.deployVerticle(eq(openAPIVerticle), any(DeploymentOptions.class)))
                .thenReturn(Future.failedFuture("boom"));

        service.deployVerticles();

        verify(vertx, never()).deployVerticle(eq(httpServerVerticle), any(DeploymentOptions.class));
    }

    @Test
    void undeploysWhenIdsPresent() {
        when(vertx.deployVerticle(eq(openAPIVerticle), any(DeploymentOptions.class)))
                .thenReturn(Future.succeededFuture("openapi"));
        when(vertx.deployVerticle(eq(httpServerVerticle), any(DeploymentOptions.class)))
                .thenReturn(Future.succeededFuture("http"));
        when(vertx.undeploy("http")).thenReturn(Future.succeededFuture());
        when(vertx.undeploy("openapi")).thenReturn(Future.succeededFuture());
        when(vertx.close()).thenReturn(Future.succeededFuture());

        service.deployVerticles();
        service.undeployVerticles();

        verify(vertx).undeploy("http");
        verify(vertx).undeploy("openapi");
        verify(vertx).close();
    }
}


