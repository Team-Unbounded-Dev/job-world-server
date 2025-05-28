package com.example.jobworldserver.user.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BulkRegisterRequest {

    @NotNull(message = "학년은 필수입니다.")
    @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
    private int grade;

    @NotNull(message = "반 번호는 필수입니다.")
    @Min(value = 1, message = "반 번호는 1 이상이어야 합니다.")
    private int classNum;

    @NotNull(message = "학생 수는 필수입니다.")
    @Min(value = 1, message = "학생 수는 1 이상이어야 합니다.")
    private int count;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "학교는 필수입니다.")
    private String school;
}