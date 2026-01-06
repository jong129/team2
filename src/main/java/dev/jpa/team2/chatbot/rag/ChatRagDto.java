package dev.jpa.team2.chatbot.rag;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import dev.jpa.team2.chatbot.embeddingchunk.EmbeddingChunkDto;

@Getter
@Setter
public class ChatRagDto {

    // ===== 요청 =====
    private Long sessionId;
    private String question;

    // ===== 응답 =====
    private Long ragId;
    private String answer;
    private List<EmbeddingChunkDto.SearchResult> references;
    private Long assistantChatId; // 응답으로 저장된 "AI 답변 메시지"의 CHAT_MESSAGE.chat_id
}
