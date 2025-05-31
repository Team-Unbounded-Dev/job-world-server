package com.example.jobworldserver.jobs.service;

import com.example.jobworldserver.jobs.dto.response.CardResponse;
import com.example.jobworldserver.jobs.entity.Card;
import com.example.jobworldserver.jobs.exception.CardNotFoundException;
import com.example.jobworldserver.jobs.mapper.CardMapper;
import com.example.jobworldserver.jobs.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository jobsCardRepository;
    private final CardMapper cardMapper;

    public List<CardResponse> getAllCards() {
        return jobsCardRepository.findAll().stream()
                .map(cardMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    public CardResponse getCardById(Long id) {
        Card card = jobsCardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));
        return cardMapper.mapToResponse(card);
    }
}