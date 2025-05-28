package com.example.jobworldserver.auth.controller;

import com.example.jobworldserver.auth.service.EmailService;
import com.example.jobworldserver.auth.service.EmailVerificationService;
import com.example.jobworldserver.auth.service.JwtService;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.dto.auth.request.EmailVerificationCheckRequest;
import com.example.jobworldserver.dto.auth.request.EmailVerificationRequest;
import com.example.jobworldserver.dto.auth.request.LoginRequest;
import com.example.jobworldserver.dto.auth.request.TokenReissueRequest;
import com.example.jobworldserver.dto.auth.response.AuthResponse;
import com.example.jobworldserver.dto.auth.response.EmailVerificationCheckResponse;
import com.example.jobworldserver.dto.auth.response.EmailVerificationResponse;
import com.example.jobworldserver.dto.auth.response.RegisterResponse;
import com.example.jobworldserver.user.dto.request.RegisterRequest;
import com.example.jobworldserver.user.service.UserService;
import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.exception.CustomException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job-world")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerificationService verificationService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<RegisterResponse>> signup(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            Long tempUserId = userService.registerTempUser(registerRequest);
            String authority = registerRequest.getAuthority() != null ? registerRequest.getAuthority().toString() : "NORMAL";
            return ResponseEntity.ok(ApiResponse.success(
                    new RegisterResponse(authority, tempUserId.toString()),
                    "임시 사용자 등록 성공. 이메일 인증을 진행해 주세요."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure("등록 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request) throws MessagingException {
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure("이미 등록된 이메일입니다.", HttpStatus.BAD_REQUEST));
        }

        Long tempUserId = Long.valueOf(request.getTempUserId());
        User tempUser = userService.findById(tempUserId)
                .orElseThrow(() -> new CustomException("임시 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        userService.updateTempUserEmail(tempUserId, request.getEmail());

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
        userService.verifyEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "이메일 인증 성공. 회원가입이 완료되었습니다. 로그인해 주세요."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.findByNickname(loginRequest.getNickname());
        if (!user.isEmailVerified()) {
            throw new CustomException("이메일 인증이 완료되지 않았습니다.", HttpStatus.FORBIDDEN);
        }
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String accessToken = jwtService.createAccessToken(user);
            String refreshToken = jwtService.createRefreshToken(user);
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } else {
            throw new CustomException("닉네임 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
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
}