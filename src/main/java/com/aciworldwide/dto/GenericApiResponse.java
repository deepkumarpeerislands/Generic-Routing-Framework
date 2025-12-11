package com.aciworldwide.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

/**
 * Generic API response wrapper.
 *
 * @param <T> the type of data being returned
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericApiResponse<T> {

    private String status;

    private String message;

    private Optional<T> data = Optional.empty();

    private Optional<Map<String, String>> errors = Optional.empty();

    /**
     * Creates a successful response with data.
     *
     * @param message the success message
     * @param data the response data
     * @param <T> the type of data
     * @return GenericApiResponse response
     */
    public static <T> GenericApiResponse<T> success(String message, T data) {
        GenericApiResponse<T> response = new GenericApiResponse<>();
        response.setStatus("success");
        response.setMessage(message);
        response.setData(Optional.ofNullable(data));
        return response;
    }

    /**
     * Creates a successful response without data.
     *
     * @param message the success message
     * @param <T> the type of data
     * @return GenericApiResponse response
     */
    public static <T> GenericApiResponse<T> success(String message) {
        GenericApiResponse<T> response = new GenericApiResponse<>();
        response.setStatus("success");
        response.setMessage(message);
        return response;
    }

    /**
     * Creates an error response.
     *
     * @param message the error message
     * @param errors the error details
     * @param <T> the type of data
     * @return GenericApiResponse response
     */
    public static <T> GenericApiResponse<T> error(String message, Map<String, String> errors) {
        GenericApiResponse<T> response = new GenericApiResponse<>();
        response.setStatus("error");
        response.setMessage(message);
        response.setErrors(Optional.ofNullable(errors));
        return response;
    }

    /**
     * Creates an error response without error details.
     *
     * @param message the error message
     * @param <T> the type of data
     * @return GenericApiResponse response
     */
    public static <T> GenericApiResponse<T> error(String message) {
        GenericApiResponse<T> response = new GenericApiResponse<>();
        response.setStatus("error");
        response.setMessage(message);
        return response;
    }
}
