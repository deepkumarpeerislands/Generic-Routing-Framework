package com.aciworldwide.config;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.vertx.core.json.jackson.DatabindCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    @Test
    void registersJdk8Module() {
        JacksonConfig config = new JacksonConfig();
        config.configureVertxJackson();

        boolean hasModule = DatabindCodec.mapper().getRegisteredModuleIds()
                .stream()
                .anyMatch(id -> id.toString().contains(Jdk8Module.class.getSimpleName()));

        assertThat(hasModule).isTrue();
    }
}


