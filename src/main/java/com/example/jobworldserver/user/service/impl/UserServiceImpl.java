package com.example.jobworldserver.user.service.impl;

import com.example.jobworldserver.auth.entity.Authority;
import com.example.jobworldserver.auth.entity.Job;
import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.user.dto.request.RegisterRequest;
import com.example.jobworldserver.exception.CustomException;
import com.example.jobworldserver.user.repository.JobRepository;
import com.example.jobworldserver.user.repository.UserRepository;
import com.example.jobworldserver.user.service.UserService;
import com.example.jobworldserver.user.validator.RegisterRequestValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterRequestValidator requestValidator;

    @PostConstruct
    public void initDefaultJobs() {
        String[] defaultJobs = {
                "초중고등학생", "대학생", "교사/강사", "학부모/보호자", "일반 직장인",
                "창작자/프리랜서", "구직 중/진로 탐색 중", "기타"
        };
        for (String jobName : defaultJobs) {
            if (!jobRepository.findByName(jobName).isPresent()) {
                jobRepository.save(Job.builder().name(jobName).build());
            }
        }
    }

    @Override
    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다: " + nickname, HttpStatus.NOT_FOUND));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public Long registerTempUser(RegisterRequest request) {
        Authority authority = request.getAuthority() != null ? request.getAuthority() : Authority.NORMAL;
        boolean isSchoolRequired = authority == Authority.TEACHER;
        boolean isJobAndAgeRequired = authority == Authority.NORMAL;

        requestValidator.validateRegisterRequest(request, isSchoolRequired, isJobAndAgeRequired);

        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new CustomException("이미 존재하는 닉네임입니다.", HttpStatus.BAD_REQUEST);
        }

        User.UserBuilder userBuilder = User.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .authority(authority)
                .emailVerified(false);

        if (authority == Authority.TEACHER) {
            userBuilder.school(request.getSchool());
        } else if (authority == Authority.NORMAL) {
            userBuilder.age(request.getAge());
            Job job = processJob(request.getJobId());
            if (job != null) {
                userBuilder.job(job);
            } else {
                throw new CustomException("직업은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST);
            }
        }

        User tempUser = userRepository.save(userBuilder.build());
        return tempUser.getId();
    }

    @Override
    @Transactional
    public void updateTempUserEmail(Long tempUserId, String email) {
        User user = userRepository.findById(tempUserId)
                .orElseThrow(() -> new CustomException("임시 사용자를 찾을 수 없습니다: " + tempUserId, HttpStatus.NOT_FOUND));
        if (userRepository.findByEmail(email).isPresent()) {
            throw new CustomException("이미 존재하는 이메일입니다.", HttpStatus.BAD_REQUEST);
        }
        user.setEmail(email);
        user.setEmailVerified(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void verifyEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("해당 이메일로 사용자를 찾을 수 없습니다: " + email, HttpStatus.NOT_FOUND));
        user.verifyEmail();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User registerOAuth2User(String email, String name, String provider) {
        if (userRepository.findByEmail(email).isPresent()) {
            return userRepository.findByEmail(email).get();
        }

        String nickname = generateUniqueNickname(name);
        User.UserBuilder userBuilder = User.builder()
                .name(name)
                .nickname(nickname)
                .password(passwordEncoder.encode(RandomStringUtils.randomAlphanumeric(16)))
                .authority(Authority.NORMAL)
                .email(email)
                .emailVerified(true)
                .provider(provider);

        return userRepository.save(userBuilder.build());
    }

    private String generateUniqueNickname(String baseName) {
        String nickname = baseName.toLowerCase().replaceAll("[^a-z0-9]", "") + RandomStringUtils.randomAlphanumeric(4);
        if (userRepository.findByNickname(nickname).isPresent()) {
            return generateUniqueNickname(baseName);
        }
        return nickname;
    }

    private Job processJob(Long jobId) {
        if (jobId != null) {
            return jobRepository.findById(jobId)
                    .orElseThrow(() -> new CustomException("해당 직업 ID가 존재하지 않습니다: " + jobId, HttpStatus.BAD_REQUEST));
        }
        return null;
    }
}