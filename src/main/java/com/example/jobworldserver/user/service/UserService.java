package com.example.jobworldserver.user.service;

import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.user.dto.request.RegisterRequest;
import java.util.Optional;

public interface UserService {
    User findByNickname(String nickname);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    Long registerTempUser(RegisterRequest request); // 임시 사용자 등록
    void updateTempUserEmail(Long tempUserId, String email); // 이메일 업데이트
    User registerOAuth2User(String email, String name, String provider);
    void verifyEmail(String email);
}