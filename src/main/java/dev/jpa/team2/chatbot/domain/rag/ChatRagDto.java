package dev.jpa.team2.chatbot.domain.rag;

import java.util.ArrayList;
import java.util.List;

import dev.jpa.team2.chatbot.api.rag.RagReferenceDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRagDto {

    // ===== 요청 =====
    private Long sessionId;
    private String question;
    
    private String docId;     // 문서 식별자
    private String docType;   // "REGISTRY" | "CONTRACT" 등
    private String stage;     // "analysis" 같은 단계
    private Integer topK;     // optional

    // ===== 응답 =====
    private Long ragId;
    private String answer;

    // Python /ask가 내려주는 근거 목록과 형태를 맞추기 위해 그대로 둔 구조
    private List<RagReferenceDto> references = new ArrayList<>();

    // Python /ask에서 내려오는 후속 질문 followUpQuestions(3개)
    private List<String> followUpQuestions = new ArrayList<>();

    // 응답으로 저장된 "AI 답변 메시지"의 CHAT_MESSAGE.chat_id
    private Long assistantChatId;

    // 세션 제목 자동 생성/업데이트 후 결과를 응답에 포함
    private String sessionTitle;

    // LLM Usage : 모델/토큰/지연 메타데이터를 응답에 포함
    private UsageDto usage;

    @Getter
    @Setter
    public static class UsageDto {
        private String model;         // CHAT_MESSAGE.MODEL
        private Integer tokensIn;     // CHAT_MESSAGE.TOKENS_IN
        private Integer tokensOut;   // CHAT_MESSAGE.TOKENS_OUT
        private Integer tokensTotal; // TOKENS_IN + TOKENS_OUT
        private Integer latencyMs;   // 응답 지연(ms)
    }
}
