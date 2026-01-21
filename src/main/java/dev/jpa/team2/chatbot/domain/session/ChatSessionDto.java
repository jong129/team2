package dev.jpa.team2.chatbot.domain.session;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

public class ChatSessionDto {

    // 세션 생성 요청 DTO
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionCreateRequest {
        private String title;
    }

    // 세션 응답 DTO : 세션 리스트용
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionItem {
        private Long sessionId;
        private String title;
        private String sessionStatus;
        private LocalDateTime lastMessageAt;

        public static SessionItem from(ChatSession s) {
            return new SessionItem(
                s.getSessionId(),
                s.getTitle(),
                s.getSessionStatus(),
                s.getLastMessageAt()
            );
        }
    }

    // 생성 응답
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionCreateResponse {
        private boolean success;
        private Long sessionId;
    }

    // 날짜별 그룹 DTO
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupedByDate<T> {
        private String date;      // "YYYY-MM-DD"
        private List<T> items;
    }

    // 검색 결과 DTO (검색 그룹에 들어가는 아이템)
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResultItem {
        private Long sessionId;
        private Long chatId;
        private String role;
        private String content;
        private LocalDateTime createdAt;
    }
}
