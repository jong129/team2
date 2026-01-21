package dev.jpa.team2.checklist.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.jpa.team2.checklist.admin.dto.ChecklistSummaryAiRequestDto;
import dev.jpa.team2.checklist.admin.dto.ChecklistSummaryAiResponseDto;
import lombok.RequiredArgsConstructor;

/**
 * FastAPI 체크리스트 요약 호출 Service / 사후 체크리스트 만족도 요약 전용
 */
@Service
@RequiredArgsConstructor
public class ChecklistSummaryAiService {

    private final RestTemplate restTemplate;

    private static final String FASTAPI_URL =
        "http://localhost:8000/checklist/summary";

    public ChecklistSummaryAiResponseDto summarize(
        ChecklistSummaryAiRequestDto req
    ) {

        return restTemplate.postForObject(
            FASTAPI_URL,
            req,
            ChecklistSummaryAiResponseDto.class
        );
    }
}
