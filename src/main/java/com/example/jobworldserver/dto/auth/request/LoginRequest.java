package com.example.jobworldserver.dto.auth.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String nickname;
    private String password;
}