package com.example.jobworldserver.domain.auth.repository;

import com.example.jobworldserver.domain.auth.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByName(String name);
}