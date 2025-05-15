package com.example.jobworldserver.controller;

import com.example.jobworldserver.domain.profile.service.ProfileService;
import com.example.jobworldserver.dto.profile.request.ProfileRequest;
import com.example.jobworldserver.dto.profile.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job-world/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{nickname}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable String nickname) {
        ProfileResponse response = profileService.getProfile(nickname);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{nickname}")
    public ResponseEntity<ProfileResponse> updateProfile(@PathVariable String nickname, @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.updateProfile(nickname, request);
        return ResponseEntity.ok(response);
    }
}