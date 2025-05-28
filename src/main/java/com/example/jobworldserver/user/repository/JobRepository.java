package com.example.jobworldserver.user.repository;

import com.example.jobworldserver.auth.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByName(String name);
    Optional<Job> findById(Long id);
}