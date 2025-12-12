package com.aciworldwide.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GenericApiResponseTest {

    @Test
    void successWithDataSetsFields() {
        GenericApiResponse<String> response = GenericApiResponse.success("ok", "payload");

        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getMessage()).isEqualTo("ok");
        assertThat(response.getData()).contains("payload");
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void successWithoutDataHasEmptyOptional() {
        GenericApiResponse<String> response = GenericApiResponse.success("done");

        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getMessage()).isEqualTo("done");
        assertThat(response.getData()).isEqualTo(Optional.empty());
    }

    @Test
    void errorWithAndWithoutDetails() {
        GenericApiResponse<String> withDetails = GenericApiResponse.error("fail", Map.of("field", "bad"));
        GenericApiResponse<String> simple = GenericApiResponse.error("oops");

        assertThat(withDetails.getStatus()).isEqualTo("error");
        assertThat(withDetails.getErrors()).contains(Optional.of(Map.of("field", "bad")).orElseThrow());
        assertThat(simple.getErrors()).isEmpty();
        assertThat(simple.getMessage()).isEqualTo("oops");
    }
}


