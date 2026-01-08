package dev.jpa.team2.chatbot.rag;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRagDto {

    // ===== 요청 =====
    private Long sessionId;
    private String question;

    // =====աձ 응답 =====
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
}
