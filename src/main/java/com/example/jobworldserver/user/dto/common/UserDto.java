package com.example.jobworldserver.user.dto.common;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String authority;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public UserDto(Long id, String email, String name, String authority, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.authority = authority;
        this.createdAt = createdAt;
    }
}