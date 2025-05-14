package com.example.jobworldserver.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 50, message = "닉네임은 최대 50자까지 허용됩니다.")
    @Pattern(regexp = "^[a-zA-Z0-9@#$%&*+-_.]+$", message = "닉네임은 영어, 숫자, 특수문자(@, #, $, %, &, *, -, _, +, .)만 허용됩니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    private String school;
    private Long jobId;
    private String customJob;

    @NotNull(message = "나이는 필수입니다.")
    private Integer age;

    private Long grade;
    private Long classNum;
    private String email;
}