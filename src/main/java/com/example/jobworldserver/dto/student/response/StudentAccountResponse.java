package com.example.jobworldserver.dto.student.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentAccountResponse {
    private String nickname;
    private String rawPassword;
}