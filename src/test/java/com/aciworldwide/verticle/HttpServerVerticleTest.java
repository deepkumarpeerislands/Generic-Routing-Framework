package com.aciworldwide.verticle;

import com.aciworldwide.registry.RouterRegistry;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
class HttpServerVerticleTest {

    @AfterEach
    void cleanup() {
        RouterRegistry.setRouter(null);
    }

    @Test
    void failsWhenRouterMissing(Vertx vertx, VertxTestContext testContext) {
        RouterRegistry.setRouter(null);
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("server.host", "localhost").put("server.port", 0));

        vertx.deployVerticle(new HttpServerVerticle(), options)
                .onComplete(ar -> {
                    testContext.verify(() -> assertThat(ar.failed()).isTrue());
                    testContext.completeNow();
                });

        vertx.setTimer(20, id -> vertx.eventBus().publish(OpenAPIVerticle.ROUTER_READY_ADDRESS, new JsonObject()));
    }

    @Test
    void startsServerWhenRouterAvailable(Vertx vertx, VertxTestContext testContext) {
        Router router = Router.router(vertx);
        RouterRegistry.setRouter(router);

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("server.host", "localhost").put("server.port", 0));

        vertx.deployVerticle(new HttpServerVerticle(), options)
                .onComplete(ar -> {
                    testContext.verify(() -> assertThat(ar.succeeded()).isTrue());
                    vertx.undeploy(ar.result()).onComplete(res -> testContext.completeNow());
                });
    }
}


