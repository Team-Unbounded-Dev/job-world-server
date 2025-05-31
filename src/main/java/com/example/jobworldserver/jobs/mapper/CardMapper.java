package com.example.jobworldserver.jobs.mapper;

import com.example.jobworldserver.jobs.dto.response.CardResponse;
import com.example.jobworldserver.jobs.entity.Card;
import com.example.jobworldserver.jobs.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CardMapper {
    public CardResponse mapToResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .imageUrl(card.getImageUrl())
                .description(card.getDescription())
                .rating(card.getRating())
                .likes(card.getLikes())
                .tags(card.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}