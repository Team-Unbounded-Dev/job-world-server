package com.example.jobworldserver.auth.jwt.constants;

public class JwtConstants {
    public static final String JWT_SECRET = "zWgYYrmkAWMHkMgjOlwV8bwO8FbNVWeJjKz9SR0M6GOBbPTb6G77mITLOXPGzFaaCTAE5CtYvGkzHzP5Aow9dA==";
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 6; // 6시간
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 7; // 7일
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
}