package com.example.jobworldserver.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User implements UserDetails, OAuth2User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = true, unique = true)
    private String email;

    @Column(nullable = true)
    private String provider;

    @Column(nullable = true)
    private String providerId;

    @Column(nullable = false)
    private boolean emailVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Authority authority;

    @Column(nullable = true)
    private String school;

    @Column(nullable = true)
    private String jobName;

    @Column(nullable = true)
    private Integer age;

    @Column(nullable = true)
    private Long grade;

    @Column(nullable = true)
    private Long classNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    private String introduction;

    @Transient
    private Map<String, Object> attributes = new HashMap<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(authority.name()));
    }

    @Override
    public String getUsername() {
        return this.id.toString(); // ID 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return this.nickname;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void updateProfile(String name, String nickname, Integer age, String jobName, String profileImageUrl, String introduction) {
        this.name = name != null ? name : this.name;
        this.nickname = nickname != null ? nickname : this.nickname;
        this.age = age != null ? age : this.age;
        this.jobName = jobName != null ? jobName : this.jobName;
        this.profileImageUrl = profileImageUrl != null ? profileImageUrl : this.profileImageUrl;
        this.introduction = introduction != null ? introduction : this.introduction;
    }
    public String getNameField() {
        return this.name;
    }
}