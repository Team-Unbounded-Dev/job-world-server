package com.example.jobworldserver.profile.controller;

import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.profile.dto.request.ProfileRequest;
import com.example.jobworldserver.profile.dto.response.ProfileResponse;
import com.example.jobworldserver.profile.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Profile", description = "사용자 프로필 관리 API")
@RestController
@RequestMapping("/job-world/profile")
@RequiredArgsConstructor
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "프로필 조회", description = "인증된 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ProfileResponse response = profileService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "프로필 조회 성공"));
    }

    @Operation(summary = "프로필 업데이트", description = "인증된 사용자의 프로필 정보를 업데이트합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 업데이트 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @RequestPart("profile") String profileJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {

        logger.debug("Received multipart request - profile JSON: {}, file: {}, contentType: {}",
                profileJson, file != null ? file.getOriginalFilename() : "null",
                file != null ? file.getContentType() : "null");

        try {
            // JSON 문자열을 ProfileRequest 객체로 변환
            ProfileRequest request = objectMapper.readValue(profileJson, ProfileRequest.class);

            User user = (User) authentication.getPrincipal();
            ProfileResponse response = profileService.updateProfile(user.getId(), request, file);
            return ResponseEntity.ok(ApiResponse.success(response, "프로필 업데이트 성공"));
        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("서버 내부 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}