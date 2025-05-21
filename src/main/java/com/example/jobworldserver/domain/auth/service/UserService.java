package com.example.jobworldserver.domain.auth.service;

import com.example.jobworldserver.domain.auth.entity.Authority;
import com.example.jobworldserver.domain.auth.entity.Job;
import com.example.jobworldserver.domain.auth.entity.User;
import com.example.jobworldserver.domain.auth.repository.JobRepository;
import com.example.jobworldserver.domain.auth.repository.UserRepository;
import com.example.jobworldserver.dto.student.response.StudentAccountResponse;
import com.example.jobworldserver.dto.user.request.RegisterRequest;
import com.example.jobworldserver.exception.CustomException.CustomException;
import com.example.jobworldserver.exception.NicknameInvalidException;
import com.example.jobworldserver.util.PasswordValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

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

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("이메일로 사용자를 찾을 수 없습니다: " + email, HttpStatus.NOT_FOUND));
    }

    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다: " + nickname, HttpStatus.NOT_FOUND));
    }

    public void registerTeacher(RegisterRequest request) {
        validateRegisterRequest(request, true, false);
        PasswordValidator.validatePassword(request.getPassword());
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new CustomException("이미 존재하는 닉네임입니다.", HttpStatus.BAD_REQUEST);
        }
        User teacher = User.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .authority(Authority.TEACHER)
                .school(request.getSchool())
                .email(request.getEmail())
                .emailVerified(false)
                .build();
        userRepository.save(teacher);
    }

    public void registerNormal(RegisterRequest request) {
        validateRegisterRequest(request, false, true);
        PasswordValidator.validatePassword(request.getPassword());
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new CustomException("이미 존재하는 닉네임입니다.", HttpStatus.BAD_REQUEST);
        }
        User normalUser = User.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .authority(Authority.NORMAL)
                .age(request.getAge())
                .email(request.getEmail())
                .emailVerified(false)
                .build();

        Job job = processJob(request.getJobId(), request.getCustomJob());
        if (job != null) {
            normalUser.setJob(job);
        } else if (request.getJobId() == null && (request.getCustomJob() == null || request.getCustomJob().trim().isEmpty())) {
            throw new CustomException("직업은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST);
        }

        userRepository.save(normalUser);
    }

    @Transactional
    public List<StudentAccountResponse> registerStudentsBulk(Long teacherId, int grade, int classNum, int count, String password, String school) {
        if (teacherId == null || grade < 1 || classNum < 1 || count < 1 || school == null || school.trim().isEmpty()) {
            throw new CustomException("모든 필드는 필수이며, 학년, 반 번호, 학생 수는 1 이상이어야 합니다.", HttpStatus.BAD_REQUEST);
        }
        PasswordValidator.validatePassword(password);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new CustomException("해당 교사를 찾을 수 없습니다. teacherId: " + teacherId, HttpStatus.NOT_FOUND));
        if (!teacher.getAuthority().equals(Authority.TEACHER)) {
            throw new CustomException("teacherId에 해당하는 사용자가 교사가 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        String randomPrefix = RandomStringUtils.randomAlphabetic(4).toUpperCase();
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
            responses.add(new StudentAccountResponse(nickname, password));
        }

        userRepository.saveAll(students);
        return responses;
    }

    private void validateRegisterRequest(RegisterRequest request, boolean isSchoolRequired, boolean isJobAndAgeRequired) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new CustomException("이름은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST);
        }
        if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
            throw new NicknameInvalidException("닉네임은 필수 입력 항목입니다.");
        }
        String nicknamePattern = "^[a-zA-Z0-9@#$%&*+-_.]+$";
        if (!request.getNickname().matches(nicknamePattern)) {
            throw new NicknameInvalidException("닉네임은 영어, 숫자, 특수문자(@, #, $, %, &, *, -, _, +, .)만 허용됩니다.");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new CustomException("비밀번호는 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST);
        }
        if (isSchoolRequired && (request.getSchool() == null || request.getSchool().trim().isEmpty())) {
            throw new CustomException("학교는 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST);
        }
        if (isJobAndAgeRequired) {
            if (request.getAge() == null || request.getAge() < 1) {
                throw new CustomException("나이는 필수이며 1 이상이어야 합니다.", HttpStatus.BAD_REQUEST);
            }
            if (request.getJobId() == null && (request.getCustomJob() == null || request.getCustomJob().trim().isEmpty())) {
                throw new CustomException("직업은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private Job processJob(Long jobId, String customJob) {
        if (jobId != null) {
            return jobRepository.findById(jobId)
                    .orElseThrow(() -> new CustomException("해당 직업 ID가 존재하지 않습니다: " + jobId, HttpStatus.BAD_REQUEST));
        } else if (customJob != null && !customJob.trim().isEmpty()) {
            return jobRepository.findByName(customJob)
                    .orElseGet(() -> {
                        Job newJob = Job.builder().name(customJob.trim()).build();
                        return jobRepository.save(newJob);
                    });
        }
        return null;
    }
}