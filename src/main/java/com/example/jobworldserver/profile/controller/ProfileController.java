package com.example.jobworldserver.profile.controller;

import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.profile.dto.request.ProfileRequest;
import com.example.jobworldserver.profile.dto.response.ProfileResponse;
import com.example.jobworldserver.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job-world/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ProfileResponse response = profileService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "프로필 조회 성공"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Valid @RequestBody ProfileRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ProfileResponse response = profileService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "프로필 업데이트 성공"));
    }
}