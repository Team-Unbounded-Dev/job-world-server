package com.example.jobworldserver.domain.auth.service;

import com.example.jobworldserver.domain.auth.entity.User;
import com.example.jobworldserver.domain.auth.repository.UserRepository;
import com.example.jobworldserver.exception.CustomException.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다: " + nickname, HttpStatus.NOT_FOUND));

        return new org.springframework.security.core.userdetails.User(
                user.getNickname(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getAuthority().name()))
        );
    }
}