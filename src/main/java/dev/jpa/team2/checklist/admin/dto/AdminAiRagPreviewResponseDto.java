package dev.jpa.team2.checklist.admin.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * ==========================================
 * RAG 기반 AI 신규 항목 제안 응답 DTO
 * - FastAPI 응답 그대로 매핑
 * ==========================================
 */
@Getter
@Setter
public class AdminAiRagPreviewResponseDto {

    private List<AiNewItemDto> newItems;

    @Getter
    @Setter
    public static class AiNewItemDto {
        private String title;
        private String description;
        private String source;
    }
}
