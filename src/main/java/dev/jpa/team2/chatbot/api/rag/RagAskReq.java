// RagAskReq.java
package dev.jpa.team2.chatbot.api.rag;

import lombok.Data;

// 역활 : 사용자가 RAG 질문을 보낼때, 컨트롤러가 받는 입력 형식을 정의
// 사용 : post  /api/rag/ask/start 같은 엔드포인트에서 request body로 들어옴

@Data
public class RagAskReq {     // 요청 DTO
    private Long sessionId;   // 어느 세션에서 질문했는지
    private String question;  // 질문 텍스트
}
