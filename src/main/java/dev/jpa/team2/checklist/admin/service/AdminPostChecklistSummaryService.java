package dev.jpa.team2.checklist.admin.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.jpa.team2.checklist.admin.dto.ChecklistSummaryAiRequestDto;
import dev.jpa.team2.checklist.admin.dto.ChecklistSummaryAiResponseDto;
import dev.jpa.team2.checklist.model.ChecklistSatisfaction;
import dev.jpa.team2.checklist.repository.ChecklistSatisfactionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 POST 체크리스트 만족도 요약 Service
 * - DB 코멘트 수집
 * - FastAPI 요약 호출
 */
@Service
@RequiredArgsConstructor
public class AdminPostChecklistSummaryService {

    private final ChecklistSatisfactionRepository satisfactionRepository;
    private final RestTemplate restTemplate;

    private static final String FASTAPI_SUMMARY_URL =
        "http://localhost:8000/checklist/summary";

    public ChecklistSummaryAiResponseDto summarize(Long templateId) {

        // 1️⃣ 만족도 코멘트 수집 (최대 100개)
        List<String> comments =
            satisfactionRepository
                .findRecentByTemplateId(templateId, PageRequest.of(0, 100))
                .stream()
                .map(ChecklistSatisfaction::getCommentText)
                .filter(c -> c != null && !c.isBlank())
                .toList();

        // 2️⃣ FastAPI 요청 DTO 생성
        ChecklistSummaryAiRequestDto req = new ChecklistSummaryAiRequestDto();
        req.setTemplateId(templateId);
        req.setComments(comments);

        // 3️⃣ FastAPI 호출
        return restTemplate.postForObject(
            FASTAPI_SUMMARY_URL,
            req,
            ChecklistSummaryAiResponseDto.class
        );
    }
}
