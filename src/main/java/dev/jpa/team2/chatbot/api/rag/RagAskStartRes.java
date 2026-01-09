// RagAskStartRes.java
package dev.jpa.team2.chatbot.api.rag;

import lombok.AllArgsConstructor;
import lombok.Data;

// 역활 : RAG 처리 시작했음을 알려주는 즉시 응답. jobId를 돌려줌
// 사용 : /ask/start 호출 직후, 프론트가 SSE를 붙거나 결과를 폴링할 떄 기준값

@Data
@AllArgsConstructor
public class RagAskStartRes {   // 시작 응답 DTO
    private String jobId;           // 이 요청의 실행 ID
}
