package com.aciworldwide.rules;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleConfigTest {

    @Test
    void storesRuleList() {
        RuleConfig config = new RuleConfig();
        RuleDefinition def = new RuleDefinition();
        config.setRules(List.of(def));

        assertThat(config.getRules()).containsExactly(def);
    }
}


