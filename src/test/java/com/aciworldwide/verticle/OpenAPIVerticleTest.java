package com.aciworldwide.verticle;

import com.aciworldwide.handler.GenericCrudHandler;
import com.aciworldwide.registry.RouterRegistry;
import com.aciworldwide.rules.SpelRuleEngine;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(VertxExtension.class)
class OpenAPIVerticleTest {

    @AfterEach
    void cleanup() {
        RouterRegistry.setRouter(null);
    }

    @Test
    void loadsSpecAndPublishesRouter(Vertx vertx, VertxTestContext testContext) {
        SpelRuleEngine ruleEngine = mock(SpelRuleEngine.class);
        GenericCrudHandler handler = new GenericCrudHandler(ruleEngine);
        String specPath = Paths.get("src/main/resources/openapi/api-spec.yaml").toUri().toString();

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("openapi.spec.path", specPath));

        MessageConsumer<JsonObject> consumer = vertx.eventBus()
                .consumer(OpenAPIVerticle.ROUTER_READY_ADDRESS);

        consumer.handler(msg -> testContext.verify(() -> {
            assertThat(msg.body().getString("status")).isEqualTo("ready");
            Router router = RouterRegistry.getRouter();
            assertThat(router).isNotNull();
            assertThat(router.getRoutes()).isNotEmpty();
            consumer.unregister();
            testContext.completeNow();
        }));

        vertx.deployVerticle(new OpenAPIVerticle(handler), options)
                .onFailure(testContext::failNow);
    }

    @Test
    void failsOnMissingSpecPath(Vertx vertx, VertxTestContext testContext) {
        GenericCrudHandler handler = new GenericCrudHandler(mock(SpelRuleEngine.class));
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject().put("openapi.spec.path", "missing.yaml"));

        vertx.deployVerticle(new OpenAPIVerticle(handler), options)
                .onComplete(ar -> {
                    testContext.verify(() -> assertThat(ar.failed()).isTrue());
                    testContext.completeNow();
                });
    }

    @Test
    void stopClearsBuilder(Vertx vertx, VertxTestContext testContext) throws Exception {
        GenericCrudHandler handler = new GenericCrudHandler(mock(SpelRuleEngine.class));
        OpenAPIVerticle verticle = new OpenAPIVerticle(handler);

        verticle.init(vertx, vertx.getOrCreateContext());
        Field builderField = OpenAPIVerticle.class.getDeclaredField("routerBuilder");
        builderField.setAccessible(true);
        builderField.set(verticle, mock(RouterBuilder.class));

        Promise<Void> stopPromise = Promise.promise();
        verticle.stop(stopPromise);
        testContext.verify(() -> assertThat(stopPromise.future().succeeded()).isTrue());

        assertThat(verticle.getRouterBuilder()).isNull();
        testContext.completeNow();
    }
}


