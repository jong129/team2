package dev.jpa.team2.checklist.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.jpa.team2.checklist.admin.dto.AdminAiImproveSummaryRequestDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiImproveSummaryResponseDto;
import lombok.RequiredArgsConstructor;

/**
 * ==========================================
 * AI 개선 요약 Service
 *
 * - "왜 이 항목이 추가되었는지" 설명 전담
 * - FastAPI RAG + LLM 호출
 * - 미리보기 Service와 책임 분리
 * ==========================================
 */
@Service
@RequiredArgsConstructor
public class AiTemplateImproveSummaryService {

    private final RestTemplate restTemplate;

    @Value("${llm.base-url}")
    private String aiServerUrl;

    /**
     * ==========================================
     * AI 개선 요약 생성
     * ==========================================
     */
    public AdminAiImproveSummaryResponseDto summarize(
            AdminAiImproveSummaryRequestDto request
    ) {

        return restTemplate.postForObject(
                aiServerUrl + "/checklist/ai/improve/summary",
                request,
                AdminAiImproveSummaryResponseDto.class
        );
    }
}
