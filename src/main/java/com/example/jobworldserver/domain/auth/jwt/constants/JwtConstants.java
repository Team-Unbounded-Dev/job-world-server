package com.example.jobworldserver.domain.auth.jwt.constants;

public class JwtConstants {
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24시간
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 14; // 14일
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}