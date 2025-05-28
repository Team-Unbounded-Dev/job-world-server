package com.example.jobworldserver.profile.dto.response;

import lombok.Data;

@Data
public class ProfileResponse {
    private String name;
    private String nickname;
    private Integer age;
    private String job;
    private String introduction;
    private String profileImageUrl;
}