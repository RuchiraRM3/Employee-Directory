package com.rapidm3.employeedirectory.dto;

import java.time.Instant;
import java.util.Map;

/** The single JSON error shape returned by every handled API error. */
public record ApiError(Instant timestamp, int status, String code, String message, Map<String, String> fieldErrors) {

    public static ApiError of(int status, String code, String message) {
        return new ApiError(Instant.now(), status, code, message, Map.of());
    }

    public static ApiError of(int status, String code, String message, Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, code, message, fieldErrors);
    }
}
