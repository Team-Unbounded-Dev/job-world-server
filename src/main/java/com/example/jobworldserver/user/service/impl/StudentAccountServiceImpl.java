package com.example.jobworldserver.user.service.impl;

import com.example.jobworldserver.auth.entity.Authority;
import com.example.jobworldserver.auth.entity.User;
import com.example.jobworldserver.user.dto.response.StudentAccountResponse;
import com.example.jobworldserver.exception.CustomException;
import com.example.jobworldserver.user.repository.UserRepository;
import com.example.jobworldserver.user.service.StudentAccountService;
import com.example.jobworldserver.user.validator.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentAccountServiceImpl implements StudentAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public List<StudentAccountResponse> createStudentAccounts(int grade, int classNum, int studentCount) {
        List<StudentAccountResponse> responses = new ArrayList<>();

        for (int i = 1; i <= studentCount; i++) {
            String rawPassword = RandomStringUtils.randomAlphanumeric(8);
            String nickname = RandomStringUtils.randomAlphabetic(4).toUpperCase() + i;

            if (rawPassword == null || rawPassword.trim().isEmpty()) {
                throw new CustomException("rawPassword 생성 실패: " + rawPassword, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (nickname == null || nickname.trim().isEmpty()) {
                throw new CustomException("nickname 생성 실패: " + nickname, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (userRepository.findByNickname(nickname).isPresent()) {
                throw new CustomException("중복 닉네임: " + nickname, HttpStatus.BAD_REQUEST);
            }

            User student = User.builder()
                    .name("학생" + i)
                    .nickname(nickname)
                    .password(passwordEncoder.encode(rawPassword))
                    .authority(Authority.STUDENT)
                    .grade((long) grade)
                    .classNum((long) classNum)
                    .emailVerified(false)
                    .build();

            userRepository.save(student);

            responses.add(new StudentAccountResponse(nickname, rawPassword, grade, classNum));
        }

        return responses;
    }

    @Override
    @Transactional
    public List<StudentAccountResponse> registerStudentsBulk(Long teacherId, int grade, int classNum, int count, String password, String school) {
        if (teacherId == null || grade < 1 || classNum < 1 || count < 1 || school == null || school.trim().isEmpty()) {
            throw new CustomException("모든 필드는 필수이며, 학년, 반 번호, 학생 수는 1 이상이어야 합니다.", HttpStatus.BAD_REQUEST);
        }
        PasswordValidator.validatePassword(password);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new CustomException("해당 교사를 찾을 수 없습니다. teacherId: " + teacherId, HttpStatus.NOT_FOUND));

        String randomPrefix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        List<StudentAccountResponse> responses = new ArrayList<>();
        List<User> students = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String nickname = String.format("%s%01d%01d%02d", randomPrefix, classNum, grade, i);

            if (userRepository.findByNickname(nickname).isPresent()) {
                throw new CustomException("중복 닉네임: " + nickname, HttpStatus.BAD_REQUEST);
            }

            User student = User.builder()
                    .name("학생" + i)
                    .nickname(nickname)
                    .password(passwordEncoder.encode(password))
                    .authority(Authority.STUDENT)
                    .school(school)
                    .grade((long) grade)
                    .classNum((long) classNum)
                    .teacher(teacher)
                    .emailVerified(false)
                    .build();

            students.add(student);
            responses.add(new StudentAccountResponse(nickname, grade, classNum, school, password));
        }

        userRepository.saveAll(students);
        return responses;
    }
}