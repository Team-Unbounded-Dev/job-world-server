package com.example.jobworldserver.jobs.repository;

import com.example.jobworldserver.jobs.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("jobsCardRepository")
public interface CardRepository extends JpaRepository<Card, Long> {
}