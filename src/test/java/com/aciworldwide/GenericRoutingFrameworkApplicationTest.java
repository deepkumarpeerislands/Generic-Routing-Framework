package com.aciworldwide;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class GenericRoutingFrameworkApplicationTest {

    @Test
    void mainStartsWithoutWebServer() {
        assertThatCode(() -> GenericRoutingFrameworkApplication.main(new String[] {"--spring.main.web-application-type=none"}))
                .doesNotThrowAnyException();
    }
}


