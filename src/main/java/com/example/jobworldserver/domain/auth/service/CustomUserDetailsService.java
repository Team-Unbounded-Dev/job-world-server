package com.example.jobworldserver.domain.auth.service;

import com.example.jobworldserver.domain.auth.entity.User;
import com.example.jobworldserver.domain.auth.repository.UserRepository;
import com.example.jobworldserver.exception.CustomException.CustomException;
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
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByNickname(username)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: {}", username);
                    return new CustomException("해당 사용자를 찾을 수 없습니다: " + username, HttpStatus.NOT_FOUND);
                });

        log.debug("사용자 로드 성공: {}", username);
        return user;
    }
}