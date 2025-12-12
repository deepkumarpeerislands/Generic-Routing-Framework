package com.aciworldwide.rules;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YamlRuleRepositoryTest {

    @Test
    void loadsRulesFromResource() {
        String yaml = """
                rules:
                  - id: a
                    entity: user
                    operationId: user.create
                    condition: "true"
                    actions:
                      - "1+1"
                """;
        YamlRuleRepository repository = new YamlRuleRepository(new ByteArrayResource(yaml.getBytes()));

        List<RuleDefinition> rules = repository.find("user", "user.create");
        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).getId()).isEqualTo("a");
    }

    @Test
    void missingRulesReturnsEmptyList() {
        String yaml = "rules: []";
        YamlRuleRepository repository = new YamlRuleRepository(new ByteArrayResource(yaml.getBytes()));

        assertThat(repository.find("missing", "op")).isEmpty();
    }
}


