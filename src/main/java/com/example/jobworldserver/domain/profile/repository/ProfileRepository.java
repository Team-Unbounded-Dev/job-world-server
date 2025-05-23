package com.example.jobworldserver.domain.profile.repository;

import com.example.jobworldserver.domain.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
}