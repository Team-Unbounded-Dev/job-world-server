package com.example.jobworldserver.dto.auth.request;

import lombok.Data;

@Data
public class TokenReissueRequest {
    private String refreshToken;
}