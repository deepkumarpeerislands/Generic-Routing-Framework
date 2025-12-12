package com.aciworldwide.service.implementation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenericCrudServiceTest {

    @Test
    void canInstantiateService() {
        GenericCrudService service = new GenericCrudService();
        assertThat(service).isNotNull();
    }
}


