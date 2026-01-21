package dev.jpa.team2.checklist.admin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import dev.jpa.team2.checklist.admin.dto.AdminAiBaseTemplateDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiBaseTemplateItemDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiImprovePreviewRequestDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiImprovePreviewResponseDto;
import dev.jpa.team2.checklist.admin.dto.AdminAiRagPreviewResponseDto;
import dev.jpa.team2.checklist.admin.dto.AiPreviewRequest;
import lombok.RequiredArgsConstructor;

/**
 * ==========================================
 * AI 개선 템플릿 "미리보기" Service (최종)
 *
 * - 기준 템플릿 조회
 * - FastAPI(RAG) 호출
 * - 기존 항목 + AI 신규 항목 병합
 * - DB 저장 없음 (미리보기 전용)
 * ==========================================
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiTemplateImprovePreviewService {

    private final AiTemplateQueryService aiTemplateQueryService;

    /* ✅ FastAPI 호출용 */
    private final RestTemplate restTemplate;

    @Value("${llm.base-url}")
    private String aiServerUrl;

    /**
     * ==========================================
     * AI 개선 템플릿 미리보기 생성
     * ==========================================
     */
    public AdminAiImprovePreviewResponseDto previewImproveTemplate(
            Long templateId,
            AdminAiImprovePreviewRequestDto request
    ) {

        /* ==========================
         * 1️⃣ 기준 템플릿 조회
         * ========================== */
        AdminAiBaseTemplateDto base =
                aiTemplateQueryService.getBaseTemplate(templateId);

        /* ==========================
         * 2️⃣ 기존 항목 title만 추출 (RAG 입력용)
         * ========================== */
        List<String> baseItemTitles = base.getItems().stream()
                .map(AdminAiBaseTemplateItemDto::getTitle)
                .collect(Collectors.toList());

        /* ==========================
         * 3️⃣ FastAPI RAG 호출
         * ========================== */
        AiPreviewRequest ragRequest = new AiPreviewRequest();
        ragRequest.setBaseItems(baseItemTitles);
        ragRequest.setPhase(base.getPhase().name());

        AdminAiRagPreviewResponseDto ragResponse =
                restTemplate.postForObject(
                        aiServerUrl + "/checklist/ai/preview",
                        ragRequest,
                        AdminAiRagPreviewResponseDto.class
                );

        /* ==========================
         * 4️⃣ 기존 항목 + AI 신규 항목 병합
         * ========================== */
        List<AdminAiBaseTemplateItemDto> mergedItems = new ArrayList<>();

        // 4-1. 기존 항목 추가
        mergedItems.addAll(base.getItems());

        // 4-2. AI 신규 항목 추가
        if (ragResponse != null && ragResponse.getNewItems() != null) {

            int nextOrder = mergedItems.size() + 1;

            for (AdminAiRagPreviewResponseDto.AiNewItemDto aiItem
                    : ragResponse.getNewItems()) {

                mergedItems.add(
                        AdminAiBaseTemplateItemDto.builder()
                                .itemOrder(nextOrder++)
                                .title(aiItem.getTitle())
                                .description(aiItem.getDescription())
                                .requiredYn(null) // 미리보기 단계 → 미정
                                .build()
                );
            }
        }

          /* ==========================
         * 6️⃣ 미리보기 DTO 반환
         * ========================== */
        return AdminAiImprovePreviewResponseDto.builder()
                .baseTemplateId(base.getTemplateId())
                .previewTemplateName(
                        base.getTemplateName() + " (AI 개선 초안)"
                )
                .previewVersionNo(base.getVersionNo() + 1)
                .items(mergedItems)
                .build();
    }
}
