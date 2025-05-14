package com.example.jobworldserver.domain.auth.service;

import com.example.jobworldserver.domain.auth.entity.Authority;
import com.example.jobworldserver.domain.auth.entity.User;
import com.example.jobworldserver.domain.auth.repository.UserRepository;
import com.example.jobworldserver.dto.student.response.StudentAccountResponse;
import com.example.jobworldserver.exception.CustomException.CustomException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class StudentAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<StudentAccountResponse> createStudentAccounts(int grade, int classNum, int studentCount) {
        List<StudentAccountResponse> responses = new ArrayList<>();

        for (int i = 1; i <= studentCount; i++) {
            String rawPassword = RandomStringUtils.randomAlphanumeric(8);
            String nickname = RandomStringUtils.randomAlphabetic(4).toUpperCase() + i;

            System.out.println("생성된 rawPassword: '" + rawPassword + "'");
            System.out.println("생성된 nickname: '" + nickname + "'");

            if (rawPassword == null || rawPassword.trim().isEmpty()) {
                throw new CustomException("rawPassword 생성 실패: " + rawPassword, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            System.out.println("생성된 nickname: '" + nickname + "'");
            if (nickname == null || nickname.trim().isEmpty()) {
                throw new CustomException("nickname 생성 실패: " + nickname, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            User student = User.builder()
                    .name("학생" + i)
                    .nickname(nickname)
                    .password(passwordEncoder.encode(rawPassword))
                    .authority(Authority.STUDENT)
                    .grade((long) grade)
                    .classNum((long) classNum)
                    .build();

            userRepository.save(student);

            responses.add(new StudentAccountResponse(nickname, rawPassword));
        }

        return responses;
    }
}