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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "인증 및 사용자 관리 API")
@RestController
@RequestMapping("/job-world")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerificationService verificationService;

    @Operation(summary = "사용자 회원가입", description = "새로운 사용자를 임시 등록하고 이메일 인증을 요청합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "임시 사용자 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
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

    @Operation(summary = "이메일 인증 요청", description = "이메일로 인증 코드를 발송합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 등록된 이메일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "임시 사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
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

    @Operation(summary = "이메일 인증 확인", description = "이메일 인증 코드를 확인하고 회원가입을 완료합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이메일 인증 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 인증 코드")
    })
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

    @Operation(summary = "사용자 로그인", description = "닉네임과 비밀번호로 로그인하여 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "잘못된 닉네임 또는 비밀번호"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "이메일 인증 미완료")
    })
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

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
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