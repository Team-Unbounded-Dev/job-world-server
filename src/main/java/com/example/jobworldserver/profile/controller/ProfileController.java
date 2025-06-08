package com.example.jobworldserver.profile.controller;

import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.profile.dto.request.ProfileRequest;
import com.example.jobworldserver.profile.dto.response.ProfileResponse;
import com.example.jobworldserver.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "사용자 프로필 관리 API")
@RestController
@RequestMapping("/job-world/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody ProfileRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ProfileResponse response = profileService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "프로필 업데이트 성공"));
    }
}