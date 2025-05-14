package com.example.jobworldserver.domain.auth.service;

import com.example.jobworldserver.domain.auth.entity.User;

public interface JwtService {
    String createAccessToken(User user);
    String createRefreshToken(User user);
    boolean validateToken(String token);
    User getUserFromToken(String token);
    boolean isTokenBlacklisted(String token);
}
