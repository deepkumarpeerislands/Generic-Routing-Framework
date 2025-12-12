package com.aciworldwide.rules;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleDefinitionTest {

    @Test
    void gettersAndSettersWork() {
        RuleDefinition def = new RuleDefinition();
        def.setId("id");
        def.setEntity("entity");
        def.setOperationId("op");
        def.setCondition("true");
        def.setActions(List.of("1"));

        assertThat(def.getId()).isEqualTo("id");
        assertThat(def.getEntity()).isEqualTo("entity");
        assertThat(def.getOperationId()).isEqualTo("op");
        assertThat(def.getCondition()).isEqualTo("true");
        assertThat(def.getActions()).containsExactly("1");
    }
}


