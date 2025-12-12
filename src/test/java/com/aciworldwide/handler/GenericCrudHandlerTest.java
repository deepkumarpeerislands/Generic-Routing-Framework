package com.aciworldwide.handler;

import com.aciworldwide.dto.GenericApiResponse;
import com.aciworldwide.rules.SpelRuleEngine;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.http.HttpServerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class GenericRoutingHandlerTest {

    private SpelRuleEngine ruleEngine;
    private GenericRoutingHandler handler;
    private RoutingContext context;
    private HttpServerResponse response;
    private RequestBody body;

    @BeforeEach
    void setUp() {
        ruleEngine = mock(SpelRuleEngine.class);
        handler = new GenericRoutingHandler(ruleEngine);
        context = mock(RoutingContext.class);
        response = mock(HttpServerResponse.class);
        body = mock(RequestBody.class);

        when(context.response()).thenReturn(response);
        when(response.setStatusCode(anyInt())).thenReturn(response);
        when(response.putHeader(any(String.class), any(String.class))).thenReturn(response);
        when(response.end(any(String.class))).thenReturn(Future.succeededFuture());
    }

    @Test
    void createEntityProduces201Response() {
        JsonObject payload = new JsonObject().put("email", "TEST@MAIL.COM");
        setupBody(payload);
        when(context.pathParam("entity")).thenReturn("users");

        handler.handle("entity.create", context);

        verify(ruleEngine).apply(eq("users"), eq("entity.create"), any(JsonObject.class));
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setStatusCode(201);
        verify(response).end(bodyCaptor.capture());

        JsonObject json = new JsonObject(bodyCaptor.getValue());
        assertThat(json.getString("status")).isEqualTo("success");
        assertThat(json.getJsonObject("data").getString("entity")).isEqualTo("users");
    }

    @Test
    void getEntityReturns200Response() {
        setupBody(new JsonObject());
        when(context.pathParam("entity")).thenReturn("orders");
        when(context.pathParam("entityId")).thenReturn("123");

        handler.handle("entity.getById", context);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setStatusCode(200);
        verify(response).end(bodyCaptor.capture());

        JsonObject json = new JsonObject(bodyCaptor.getValue());
        assertThat(json.getJsonObject("data").getString("id")).isEqualTo("123");
    }

    @Test
    void missingOperationIdFailsFast() {
        setupBody(new JsonObject());

        handler.handle("", context);

        verify(response).setStatusCode(400);
        verify(response).end(any(String.class));
    }

    @Test
    void unsupportedOperationReturns501() {
        setupBody(new JsonObject());
        when(context.pathParam("entity")).thenReturn("users");

        handler.handle("entity.update", context);

        verify(response).setStatusCode(501);
        verify(response).end(any(String.class));
    }

    @Test
    void safeBodyHandlesNull() {
        when(context.body()).thenReturn(null);
        when(context.pathParam("entity")).thenReturn("users");

        handler.handle("entity.create", context);

        verify(ruleEngine).apply(eq("users"), eq("entity.create"), any(JsonObject.class));
    }

    private void setupBody(JsonObject payload) {
        Buffer buffer = payload.toBuffer();
        when(context.body()).thenReturn(body);
        when(body.buffer()).thenReturn(buffer);
        when(body.asJsonObject()).thenReturn(payload);
    }
}


