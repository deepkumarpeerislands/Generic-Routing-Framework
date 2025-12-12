package com.aciworldwide.config;

import com.aciworldwide.handler.GenericCrudHandler;
import com.aciworldwide.rules.SpelRuleEngine;
import com.aciworldwide.verticle.HttpServerVerticle;
import com.aciworldwide.verticle.OpenAPIVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class VertxConfigTest {

    private final VertxConfig config = new VertxConfig();

    @Test
    void buildsVertxAndConfigObjects() throws Exception {
        setField("serverHost", "127.0.0.1");
        setField("serverPort", 8081);
        setField("serverTimeout", 5000L);
        setField("openApiSpecPath", "spec.yaml");

        Vertx vertx = config.vertx();
        JsonObject serverConfig = config.serverConfig();

        assertThat(vertx).isNotNull();
        assertThat(serverConfig.getString("server.host")).isEqualTo("127.0.0.1");
        assertThat(serverConfig.getInteger("server.port")).isEqualTo(8081);
        vertx.close().toCompletionStage().toCompletableFuture().join();
    }

    @Test
    void createsVerticleBeans() throws Exception {
        setField("serverHost", "localhost");
        setField("serverPort", 8080);
        setField("serverTimeout", 1000L);
        setField("openApiSpecPath", "spec.yaml");

        GenericCrudHandler handler = config.genericHandler(mock(SpelRuleEngine.class));
        HttpServerVerticle httpVerticle = config.httpServerVerticle(config.serverConfig());
        OpenAPIVerticle openApiVerticle = config.openAPIVerticle(handler);

        assertThat(handler).isNotNull();
        assertThat(httpVerticle).isNotNull();
        assertThat(openApiVerticle).isNotNull();
    }

    private void setField(String name, Object value) throws Exception {
        Field field = VertxConfig.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(config, value);
    }
}


