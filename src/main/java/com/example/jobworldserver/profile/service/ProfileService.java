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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileRequest request, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다: userId=" + userId, HttpStatus.NOT_FOUND));

        if (!user.getNickname().equals(request.getNickname()) &&
                userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new CustomException("이미 사용 중인 닉네임입니다.", HttpStatus.BAD_REQUEST);
        }

        String profileImageUrl = user.getProfileImageUrl();
        if (file != null && !file.isEmpty()) {
            logger.debug("업로드된 파일 크기: {} bytes", file.getSize());
            try {
                if (file.getSize() > 5 * 1024 * 1024) {
                    throw new CustomException("이미지 크기는 5MB를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST);
                }
                String fileName = user.getNickname() + "_profile_" + Instant.now().toEpochMilli() + "." + file.getOriginalFilename().split("\\.")[1];
                profileImageUrl = fileStorageService.storeFile(fileName, file);
            } catch (IOException e) {
                throw new CustomException("파일 처리 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
        response.setName(user.getNameField()); // 새로운 getter 사용
        response.setNickname(user.getNickname());
        response.setAge(user.getAge());
        response.setJob(user.getJobName());
        response.setIntroduction(user.getIntroduction());
        response.setProfileImageUrl(user.getProfileImageUrl());
        return response;
    }
}