package com.example.jobworldserver.dto.profile.response;

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
