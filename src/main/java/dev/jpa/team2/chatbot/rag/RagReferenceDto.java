package dev.jpa.team2.chatbot.rag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RagReferenceDto {
    // ✅ Chroma ID는 보통 "doc_xxx" 같은 문자열이라 String 권장
    private String chunkId;

    private String title;   // meta.title을 쓰면 들어오고, 없으면 null
    private String snippet; // 근거 미리보기 텍스트
}
