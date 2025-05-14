package com.example.jobworldserver.domain.auth.jwt.exception;

import com.example.jobworldserver.exception.CustomException.CustomException;
import org.springframework.http.HttpStatus;

public class JwtException extends CustomException {
    public JwtException(String message, HttpStatus status) {
        super(message, status);
    }

    public static JwtException UNAUTHORIZED(String message) {
        return new JwtException(message, HttpStatus.UNAUTHORIZED);
    }

    public static JwtException FORBIDDEN(String message) {
        return new JwtException(message, HttpStatus.FORBIDDEN);
    }
}