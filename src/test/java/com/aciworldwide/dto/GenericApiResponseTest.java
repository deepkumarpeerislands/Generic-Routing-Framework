package com.aciworldwide.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GenericApiResponseTest {

    @Test
    void successWithDataSetsFields() {
        GenericApiResponse<String> response = GenericApiResponse.success("ok", "payload", 200);

        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getMessage()).isEqualTo("ok");
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getData()).contains("payload");
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void successWithoutDataHasEmptyOptional() {
        GenericApiResponse<String> response = GenericApiResponse.success("done", 201);

        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getMessage()).isEqualTo("done");
        assertThat(response.getStatusCode()).isEqualTo(201);
        assertThat(response.getData()).isEqualTo(Optional.empty());
    }

    @Test
    void errorWithAndWithoutDetails() {
        GenericApiResponse<String> withDetails = GenericApiResponse.error("fail", Map.of("field", "bad"), 400);
        GenericApiResponse<String> simple = GenericApiResponse.error("oops", 500);

        assertThat(withDetails.getStatus()).isEqualTo("error");
        assertThat(withDetails.getStatusCode()).isEqualTo(400);
        assertThat(withDetails.getErrors()).contains(Optional.of(Map.of("field", "bad")).orElseThrow());
        assertThat(simple.getErrors()).isEmpty();
        assertThat(simple.getMessage()).isEqualTo("oops");
        assertThat(simple.getStatusCode()).isEqualTo(500);
    }
}


