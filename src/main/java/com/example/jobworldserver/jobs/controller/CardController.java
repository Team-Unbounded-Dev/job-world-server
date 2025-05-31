package com.example.jobworldserver.jobs.controller;

import com.example.jobworldserver.dto.auth.common.ApiResponse;
import com.example.jobworldserver.jobs.dto.response.CardResponse;
import com.example.jobworldserver.jobs.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Cards", description = "카드 조회 API")
@RestController
@RequestMapping("/job-world/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @Operation(summary = "카드 목록 조회", description = "모든 카드 목록을 조회합니다. (권한: TEACHER, STUDENT, NORMAL)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카드 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 부족")
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'NORMAL')")
    public ResponseEntity<com.example.jobworldserver.dto.auth.common.ApiResponse<List<CardResponse>>> getCards() {
        List<CardResponse> cards = cardService.getAllCards();
        return ResponseEntity.ok(ApiResponse.success(cards, "카드 목록 조회 성공"));
    }

    @Operation(summary = "단일 카드 조회", description = "특정 ID의 카드를 조회합니다. (권한: TEACHER, STUDENT, NORMAL)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카드 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 부족"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카드를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'NORMAL')")
    public ResponseEntity<ApiResponse<CardResponse>> getCardById(@PathVariable Long id) {
        CardResponse cardResponse = cardService.getCardById(id);
        return ResponseEntity.ok(ApiResponse.success(cardResponse, "카드 조회 성공"));
    }
}