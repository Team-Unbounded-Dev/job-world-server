package com.example.jobworldserver.controller;

import com.example.jobworldserver.domain.auth.entity.User;
import com.example.jobworldserver.domain.auth.service.JwtService;
import com.example.jobworldserver.domain.auth.service.UserService;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.dto.user.common.UserDto;
import com.example.jobworldserver.dto.auth.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/job-world/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final JwtService jwtService;
    private final UserService userService;

    @GetMapping("/user-info")
    public ResponseEntity<ApiResponse<UserDto>> getUserInfo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증된 사용자 정보가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED));
        }

        Object principal = authentication.getPrincipal();
        User user;

        if (principal instanceof User) {
            user = (User) principal;
        } else if (principal instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String provider = oAuth2User.getAttribute("provider");

            if (email == null || name == null) {
                log.warn("OAuth2User에서 필수 속성을 찾을 수 없음: email={}, name={}", email, name);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.failure("사용자 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));
            }

            user = userService.findByEmail(email).orElseGet(() -> {
                log.info("새로운 OAuth2 사용자 등록: email={}", email);
                return userService.registerOAuth2User(email, name, provider);
            });
        } else {
            log.warn("알 수 없는 인증 객체: {}", principal.getClass().getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED));
        }

        if (user == null) {
            log.warn("사용자를 찾을 수 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));
        }

        log.info("OAuth2 사용자 정보 요청: email={}", user.getEmail());
        UserDto userDto = new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAuthority().name(),
                null
        );
        return ResponseEntity.ok(ApiResponse.success(userDto, "사용자 정보 조회 성공"));
    }

    @GetMapping("/token")
    public ResponseEntity<ApiResponse<AuthResponse>> getToken(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증된 사용자 정보가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED));
        }

        Object principal = authentication.getPrincipal();
        User user;

        if (principal instanceof User) {
            user = (User) principal;
        } else if (principal instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String provider = oAuth2User.getAttribute("provider");

            if (email == null || name == null) {
                log.warn("OAuth2User에서 필수 속성을 찾을 수 없음: email={}, name={}", email, name);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.failure("사용자 정보를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));
            }

            user = userService.findByEmail(email).orElseGet(() -> {
                log.info("새로운 OAuth2 사용자 등록: email={}", email);
                return userService.registerOAuth2User(email, name, provider);
            });
        } else {
            log.warn("알 수 없는 인증 객체: {}", principal.getClass().getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED));
        }

        if (user == null) {
            log.warn("사용자를 찾을 수 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));
        }

        log.info("OAuth2 토큰 발급 요청: email={}", user.getEmail());
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);
        log.info("토큰 발급 완료: 사용자={}", user.getEmail());

        return ResponseEntity.ok(ApiResponse.success(
                new AuthResponse(accessToken, refreshToken),
                "토큰 발급 성공"));
    }
}