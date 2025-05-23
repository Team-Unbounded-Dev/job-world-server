package com.example.jobworldserver.exception;

import com.example.jobworldserver.exception.CustomException.CustomException;
import org.springframework.http.HttpStatus;

public class NicknameInvalidException extends CustomException {
    public NicknameInvalidException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}