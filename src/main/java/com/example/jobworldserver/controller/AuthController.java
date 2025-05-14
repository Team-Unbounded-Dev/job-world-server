package com.example.jobworldserver.controller;

import com.example.jobworldserver.domain.auth.service.EmailVerificationService;
import com.example.jobworldserver.domain.auth.service.UserService;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.dto.auth.request.EmailVerificationCheckRequest;
import com.example.jobworldserver.dto.auth.request.EmailVerificationRequest;
import com.example.jobworldserver.dto.auth.response.AuthResponse;
import com.example.jobworldserver.dto.auth.request.LoginRequest;
import com.example.jobworldserver.dto.auth.response.EmailVerificationCheckResponse;
import com.example.jobworldserver.dto.auth.response.EmailVerificationResponse;
import com.example.jobworldserver.dto.user.request.RegisterRequest;
import com.example.jobworldserver.domain.auth.entity.User;
import com.example.jobworldserver.domain.auth.service.JwtService;
import com.example.jobworldserver.dto.auth.request.TokenReissueRequest;
import com.example.jobworldserver.exception.CustomException.CustomException;
import com.example.jobworldserver.domain.auth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/job-world")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerificationService verificationService;


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.findByNickname(loginRequest.getNickname());
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String accessToken = jwtService.createAccessToken(user);
            String refreshToken = jwtService.createRefreshToken(user);
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } else {
            throw new CustomException("닉네임 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register/teacher")
    public ResponseEntity<String> registerTeacher(@RequestBody RegisterRequest registerRequest) {
        try {
            userService.registerTeacher(registerRequest);
            return ResponseEntity.ok("교사 등록 성공");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("등록 실패: " + e.getMessage());
        }
    }

    @PostMapping("/register/normal")
    public ResponseEntity<String> registerNormal(@RequestBody RegisterRequest registerRequest) {
        try {
            userService.registerNormal(registerRequest);
            return ResponseEntity.ok("일반 사용자 등록 성공");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("등록 실패: " + e.getMessage());
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<AuthResponse> reissue(@RequestBody TokenReissueRequest request) {
        if (!jwtService.validateToken(request.getRefreshToken())) {
            throw new CustomException("유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED);
        }

        User user = jwtService.getUserFromToken(request.getRefreshToken());
        String newAccessToken = jwtService.createAccessToken(user);
        String newRefreshToken = jwtService.createRefreshToken(user);
        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
    }


    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request) throws MessagingException {
        EmailVerificationResponse response = emailService.verifyEmail(request.getEmail());
        if (response.getVerificationCode() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(response.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
        return ResponseEntity.ok(ApiResponse.success(response, "이메일 인증 요청 성공"));
    }
    @PostMapping("/check-verification")
    public ResponseEntity<ApiResponse<EmailVerificationCheckResponse>> checkVerification(
            @Valid @RequestBody EmailVerificationCheckRequest request) {
        EmailVerificationCheckResponse response = verificationService.checkVerificationCode(
                request.getEmail(), request.getVerificationCode());
        if (!response.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(response.getMessage(), HttpStatus.BAD_REQUEST));
        }
        return ResponseEntity.ok(ApiResponse.success(response, "인증 성공"));
    }
}