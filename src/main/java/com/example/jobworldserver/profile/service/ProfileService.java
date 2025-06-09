package com.example.jobworldserver.profile.service;

import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.profile.dto.request.ProfileRequest;
import com.example.jobworldserver.profile.dto.response.ProfileResponse;
import com.example.jobworldserver.exception.CustomException;
import com.example.jobworldserver.user.repository.UserRepository;
import com.example.jobworldserver.profile.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다: userId=" + userId, HttpStatus.NOT_FOUND));

        if (!user.getNickname().equals(request.getNickname()) &&
                userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new CustomException("이미 사용 중인 닉네임입니다.", HttpStatus.BAD_REQUEST);
        }

        String profileImageUrl = user.getProfileImageUrl();
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            String base64Image = extractBase64Data(request.getProfileImage());
            logger.debug("추출된 Base64 데이터 길이: {}", base64Image.length());
            try {
                byte[] imageData = java.util.Base64.getDecoder().decode(base64Image);
                if (imageData.length > 5 * 1024 * 1024) {
                    throw new CustomException("이미지 크기는 5MB를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST);
                }
                String fileName = user.getNickname() + "_profile_" + Instant.now().toEpochMilli() + ".jpg";
                profileImageUrl = fileStorageService.storeFile(fileName, imageData);
            } catch (IllegalArgumentException e) {
                throw new CustomException("잘못된 Base64 형식입니다: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (RuntimeException e) {
                throw new CustomException("파일 저장 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        String jobName = user.getJobName();
        if (request.getJob() != null) {
            jobName = request.getJob();
        }

        user.updateProfile(
                request.getName(),
                request.getNickname(),
                request.getAge(),
                jobName,
                profileImageUrl,
                request.getIntroduction()
        );

        userRepository.save(user);

        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다: userId=" + userId, HttpStatus.NOT_FOUND));
        return mapToResponse(user);
    }

    private ProfileResponse mapToResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setName(user.getName());
        response.setNickname(user.getNickname());
        response.setAge(user.getAge());
        response.setJob(user.getJobName());
        response.setIntroduction(user.getIntroduction());
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