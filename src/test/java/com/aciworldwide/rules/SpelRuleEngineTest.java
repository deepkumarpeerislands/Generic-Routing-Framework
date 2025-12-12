package com.aciworldwide.rules;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpelRuleEngineTest {

    @Test
    void appliesMatchingRuleActions() {
        RuleDefinition definition = new RuleDefinition();
        definition.setEntity("user");
        definition.setOperationId("user.create");
        definition.setCondition("#payload.getInteger('age') > 17");
        definition.setActions(List.of("#payload.put('approved', true)"));

        YamlRuleRepository repository = new YamlRuleRepository(new ByteArrayResource("rules: []".getBytes())) {
            @Override
            public List<RuleDefinition> find(String entity, String operationId) {
                return List.of(definition);
            }
        };

        SpelRuleEngine engine = new SpelRuleEngine(repository);
        JsonObject payload = new JsonObject().put("age", 20);

        engine.apply("user", "user.create", payload);

        assertThat(payload.getBoolean("approved")).isTrue();
    }

    @Test
    void skipsWhenNoRules() {
        YamlRuleRepository repository = new YamlRuleRepository(new ByteArrayResource("rules: []".getBytes()));
        SpelRuleEngine engine = new SpelRuleEngine(repository);
        JsonObject payload = new JsonObject().put("age", 10);

        engine.apply("user", "user.create", payload);

        assertThat(payload.containsKey("status")).isFalse();
    }
}


