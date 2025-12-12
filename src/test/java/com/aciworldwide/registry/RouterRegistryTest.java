package com.aciworldwide.registry;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RouterRegistryTest {

    @AfterEach
    void clear() {
        RouterRegistry.setRouter(null);
    }

    @Test
    void storesAndReturnsRouter() {
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);

        RouterRegistry.setRouter(router);

        assertThat(RouterRegistry.getRouter()).isSameAs(router);
        vertx.close().toCompletionStage().toCompletableFuture().join();
    }
}


