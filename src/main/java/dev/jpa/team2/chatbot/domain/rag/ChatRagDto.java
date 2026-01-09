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

    // ===== 응답 =====
    private Long ragId;
    private String answer;

    // ✅ Python /ask에서 내려오는 references 형태에 맞춤
    private List<RagReferenceDto> references = new ArrayList<>();

    // ✅ Python /ask에서 내려오는 followUpQuestions(3개)
    private List<String> followUpQuestions = new ArrayList<>();

    // 응답으로 저장된 "AI 답변 메시지"의 CHAT_MESSAGE.chat_id
    private Long assistantChatId;

    // ✅ 세션 제목(자동 생성된 최신 title을 응답에 포함)
    private String sessionTitle;

    // =========================
    // ✅ LLM Usage (토큰/지연)
    // =========================
    private UsageDto usage;

    @Getter
    @Setter
    public static class UsageDto {
        private String model;        // CHAT_MESSAGE.MODEL
        private Integer tokensIn;    // CHAT_MESSAGE.TOKENS_IN
        private Integer tokensOut;   // CHAT_MESSAGE.TOKENS_OUT
        private Integer tokensTotal; // (권장) TOKENS_IN + TOKENS_OUT
        private Integer latencyMs;   // (권장) 응답 지연(ms)
    }
}
