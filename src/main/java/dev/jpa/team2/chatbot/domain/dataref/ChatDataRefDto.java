package dev.jpa.team2.chatbot.domain.dataref;

import lombok.*;

public class ChatDataRefDto {

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String refType;  // DOCUMENT_ANALYSIS / CHECKLIST_RESULT ...
        private String title;    // "문서 분석 결과"
        private String summary;  // 요약 텍스트
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {  // 어느 세션에 붙었는지 / 저장된 refId가 뭔지 프론트에서 추적 가능
        private boolean success;
        private Long sessionId;
        private Long refId;
    }
}
