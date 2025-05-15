package com.example.jobworldserver.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Authority authority;

    @Column(nullable = true)
    private String school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @Column(nullable = true)
    private Integer age;

    @Column(nullable = true)
    private Long grade;

    @Column(nullable = true)
    private Long classNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Column(unique = true, nullable = true)
    private String email;

    @Builder.Default
    private Boolean emailVerified = false;

    @Column(nullable = true)
    private String profileImageUrl;

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public void updateProfile(String name, String nickname, Integer age, Job job, String introduction, String profileImageUrl) {
        this.name = name;
        this.nickname = nickname;
        this.age = age;
        this.job = job;
        this.profileImageUrl = profileImageUrl;
    }
}