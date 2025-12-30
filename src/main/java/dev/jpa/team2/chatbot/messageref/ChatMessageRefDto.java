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
    private String mode;        // "CHAT_REFS" | "STATS"
    private String scope;       // "MY" | "ALL" | null

    // 특정 메시지(chatId)의 근거
    private Long chatId;                    // 대상 메시지
    private Integer refCount;               // refs 개수
    private List<RefItem> refs;             // 근거 목록

    // 품질 분석 통계
    private Integer days;                   // 최근 N일
    private Long totalRefs;                 // 전체 ref 개수
    private Double avgScore;                // 평균 score
    private List<TopChunkStat> topChunks;   // 자주 쓰인 chunk


    // 내부 DTO들
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefItem {
        private Long refId;
        private Long chatId;
        private Long chunkId;
        private Double score;
        private LocalDateTime createdAt;

        public static RefItem from(ChatMessageRef e) {
            return RefItem.builder()
                .refId(e.getRefId())
                .chatId(e.getChatId())
                .chunkId(e.getChunkId())
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
    }
}
