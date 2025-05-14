package com.example.jobworldserver.dto.auth.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private int status;

    // 성공 응답 생성자
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = message;
        response.status = HttpStatus.OK.value();
        return response;
    }

    // 실패 응답 생성자
    public static <T> ApiResponse<T> failure(String message, HttpStatus status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.data = null;
        response.message = message;
        response.status = status.value();
        return response;
    }
}