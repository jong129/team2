package dev.jpa.team2.chatbot.messageref;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRefDto {

    // 공통 메타 정보
    private String mode;        // "CHAT_REFS" | "STATS" | "BAD_CHUNKS"
    private String scope;       // "MY" | "ALL" | null

    // 특정 메시지(chatId)의 근거
    private Long chatId;
    private Integer refCount;
    private List<RefItem> refs;

    // 품질 분석 통계
    private Integer days;
    private Long totalRefs;
    private Double avgScore;
    private List<TopChunkStat> topChunks;

    // 나쁜 답변 기반 문제 chunk 후보
    private Integer badDislikeN;              // dislike_count >= N
    private List<TopChunkStat> badChunks;     // 후보 chunk 목록(TopChunkStat 재사용)

    // -----------------------------
    // 내부 DTO들
    // -----------------------------
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefItem {
        private Long messageRefId;
        private Long chatId;
        private Long chunkId;
        private Integer rankNo;           // ✅ 추가
        private Double score;
        private LocalDateTime createdAt;

        public static RefItem from(ChatMessageRef e) {
            return RefItem.builder()
                .messageRefId(e.getMessageRefId())
                .chatId(e.getChatId())
                .chunkId(e.getChunkId())
                .rankNo(e.getRankNo())    // ✅ 추가 (엔티티에 rankNo 필요)
                .score(e.getScore())
                .createdAt(e.getCreatedAt())
                .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopChunkStat {
        private Long chunkId;
        private Long count;
        private Double avgScore;

        // ✅ dislike 연관 통계
        private Long sumDislikes;       // 해당 chunk가 포함된 답변들의 dislike 합
        private Long badAnswerCount;    // dislike_count >= N 답변에서의 등장 횟수
    }
}
