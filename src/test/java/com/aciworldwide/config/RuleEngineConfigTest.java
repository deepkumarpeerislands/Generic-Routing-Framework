package com.aciworldwide.config;

import com.aciworldwide.rules.SpelRuleEngine;
import com.aciworldwide.rules.YamlRuleRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineConfigTest {

    @Test
    void createsBeans() {
        RuleEngineConfig config = new RuleEngineConfig();
        YamlRuleRepository repository = config.yamlRuleRepository();
        SpelRuleEngine engine = config.spelRuleEngine(repository);

        assertThat(repository).isNotNull();
        assertThat(engine).isNotNull();
    }
}


