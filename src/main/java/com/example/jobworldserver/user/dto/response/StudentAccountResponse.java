package com.example.jobworldserver.user.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAccountResponse {
    private String nickname;
    private Integer grade;
    private Integer classNum;
    private String school;
    private String password;

    public StudentAccountResponse(String nickname, String password, Integer grade, Integer classNum) {
        this.nickname = nickname;
        this.password = password;
        this.grade = grade;
        this.classNum = classNum;
    }
}