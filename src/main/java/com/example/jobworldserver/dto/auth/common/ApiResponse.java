package com.example.jobworldserver.dto.auth.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final int status;
    private final LocalDateTime timestamp;
    private final T data;

    private ApiResponse(boolean success, String message, int status, T data) {
        this.success = success;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, HttpStatus.OK.value(), data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, HttpStatus.OK.value(), null);
    }

    public static <T> ApiResponse<T> failure(String message, HttpStatus status) {
        return new ApiResponse<>(false, message, status.value(), null);
    }
}