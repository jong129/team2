package dev.jpa.team2.chatbot.api.rag;

import lombok.Getter;
import lombok.Setter;

// 역활 : RAG 답변에 딸려오는 근거 1건을 표현 => 어떤 문서/청크에서 이 답이 나왔는지 보여주는 구조
// 사용 : 최종 답변 응답(또는 SSE 완료 이벤트)에서 reference: [] 형태로 포함

@Getter
@Setter
public class RagReferenceDto {
    // Chroma ID는 보통 "doc_xxx" 같은 문자열이라 String 권장
    private String chunkId;

    private String title;        // meta.title을 쓰면 들어오고, 없으면 null
    private String snippet;   // 근거 텍스트 일부
    
    private Double score;    // 유사도 점수
    private Integer rankNo; 
}
