package com.example.jobworldserver.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileRequest {
    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 50, message = "닉네임은 50자 이내여야 합니다.")
    private String nickname;

    private Integer age;

    private String job;

    @Size(max = 500, message = "소개글은 500자 이내여야 합니다.")
    private String introduction;
}