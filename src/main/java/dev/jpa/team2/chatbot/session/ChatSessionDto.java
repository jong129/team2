package dev.jpa.team2.chatbot.session;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

public class ChatSessionDto {

    // =========================
    // 1) 세션 생성 요청 DTO (기존 ChatSessionCreateDto)
    // =========================
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionCreateRequest {
        private String title;
    }

    // =========================
    // 2) 세션 응답 DTO (기존 ChatSessionDto)
    // =========================
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

    // =========================
    // (선택) 생성 응답
    // =========================
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionCreateResponse {
        private boolean success;
        private Long sessionId;
    }

    // =========================
    // 3) 날짜별 그룹 DTO (기존 GroupedSessionsDto, GroupedSearchResultsDto 공통)
    // =========================
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupedByDate<T> {
        private String date;      // "YYYY-MM-DD"
        private List<T> items;
    }

    // =========================
    // 4) 검색 결과 DTO (검색 그룹에 들어가는 아이템)
    //    ※ 너 프로젝트에 이미 SearchResultDto가 있으면 이건 빼고 그걸 써도 됨
    // =========================
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
