package com.example.jobworldserver.auth.service;

import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.user.repository.UserRepository;
import com.example.jobworldserver.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userId) {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: userId={}", userId);
                    return new CustomException("해당 사용자를 찾을 수 없습니다: " + userId, HttpStatus.NOT_FOUND);
                });

        log.debug("사용자 로드 성공: userId={}", userId);
        return user;
    }
}