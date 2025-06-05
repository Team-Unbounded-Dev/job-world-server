package com.example.jobworldserver.user.validator;

import com.example.jobworldserver.user.dto.request.RegisterRequest;
import com.example.jobworldserver.exception.CustomException;
import com.example.jobworldserver.exception.NicknameInvalidException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RegisterRequestValidator {

    public void validateRegisterRequest(RegisterRequest request, boolean isSchoolRequired, boolean isJobAndAgeRequired) {
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
            if (request.getJobId() == null) {
                throw new CustomException("직업은 필수 입력 항목입니다.", HttpStatus.BAD_REQUEST);
            }
        }
    }
}