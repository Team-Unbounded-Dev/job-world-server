package com.example.jobworldserver.dto.auth.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailVerificationCheckResponse {
    private boolean isValid;
    private String message;
}