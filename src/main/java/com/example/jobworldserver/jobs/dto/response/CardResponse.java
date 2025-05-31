package com.example.jobworldserver.jobs.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private Double rating;
    private Long likes;
    private List<String> tags;
}