package com.example.jobworldserver.domain.profile.service;

import com.example.jobworldserver.domain.profile.repository.ProfileRepository;
import com.example.jobworldserver.domain.profile.service.storage.FileStorageService;
import com.example.jobworldserver.dto.profile.request.ProfileRequest;
import com.example.jobworldserver.dto.profile.response.ProfileResponse;
import com.example.jobworldserver.domain.auth.entity.Job;
import com.example.jobworldserver.domain.auth.entity.User;
import com.example.jobworldserver.domain.auth.repository.JobRepository;
import com.example.jobworldserver.exception.CustomException.CustomException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final JobRepository jobRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProfileResponse updateProfile(String nickname, ProfileRequest request) {
        User user = profileRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다: " + nickname, HttpStatus.NOT_FOUND));

        String profileImageUrl = null;
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            String base64Image = extractBase64Data(request.getProfileImage());
            logger.debug("추출된 Base64 데이터: {}", base64Image);
            try {
                byte[] imageData = java.util.Base64.getDecoder().decode(base64Image);
                profileImageUrl = fileStorageService.storeFile(nickname + "_profile.jpg", imageData);
            } catch (IllegalArgumentException e) {
                throw new CustomException("잘못된 Base64 형식입니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        Job job = null;
        if (request.getJob() != null && !request.getJob().trim().isEmpty()) {
            job = jobRepository.findByName(request.getJob())
                    .orElseGet(() -> {
                        Job newJob = Job.builder().name(request.getJob()).build();
                        return jobRepository.save(newJob);
                    });
        }

        user.updateProfile(
                request.getName(),
                request.getNickname(),
                request.getAge(),
                job,
                profileImageUrl
        );

        profileRepository.save(user);

        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String nickname) {
        User user = profileRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다: " + nickname, HttpStatus.NOT_FOUND));
        return mapToResponse(user);
    }

    private ProfileResponse mapToResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setName(user.getName());
        response.setNickname(user.getNickname());
        response.setAge(user.getAge());
        response.setJob(user.getJob() != null ? user.getJob().getName() : null);
        response.setIntroduction(null);
        response.setProfileImageUrl(user.getProfileImageUrl());
        return response;
    }

    private String extractBase64Data(String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            return null;
        }

        String prefixPattern = "data:image/[^;]+;base64,";
        if (imageData.matches(prefixPattern + "[A-Za-z0-9+/=]+")) {
            String[] parts = imageData.split(",");
            if (parts.length != 2) {
                throw new CustomException("Base64 데이터 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
            }
            return parts[1].trim();
        }

        if (imageData.matches("[A-Za-z0-9+/=]+")) {
            return imageData.trim();
        }
        throw new CustomException("Base64 데이터 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
    }
}