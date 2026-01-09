package dev.jpa.team2.chatbot.dataref;

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
    public static class Response {
        private boolean success;
        private Long sessionId;
        private Long refId;
    }
}
