package dev.jpa.team2.board_ai;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiWriteDraftResponse {

    private Long draftId;        // BOARD_AI_DRAFT.DRAFT_ID
    private Long categoryId;     // BOARD_AI_DRAFT.CATEGORY_ID

    private String aiType;       // "WRITE"
    private String resultText;   // AI 생성 결과(초안)

    private String promptCode;   // 사용한 프롬프트 코드
    private String modelName;    // 사용한 모델명

    private boolean cached;      // 캐시 결과 여부
}
