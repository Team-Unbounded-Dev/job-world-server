package com.example.jobworldserver.auth.service;

import com.example.jobworldserver.auth.entity.User;

public interface JwtService {
    String createAccessToken(User user);
    String createRefreshToken(User user);
    boolean validateToken(String token);
    User getUserFromToken(String token);
    boolean isTokenBlacklisted(String token);
    String getUserNicknameFromToken(String token);
    Long getUserIdFromToken(String token);
}