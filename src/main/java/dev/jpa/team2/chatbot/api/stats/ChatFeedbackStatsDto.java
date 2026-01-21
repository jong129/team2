package dev.jpa.team2.chatbot.api.stats;

import java.util.List;
import lombok.*;

// 챗봇 응답 품질(좋아요/싫어요) 통계 DTO
// 사용자 피드백(좋아요/싫어요)을 기반으로, 모델별 품질과 문제 응답을 분석하기 위한 관리자 통계 응답

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatFeedbackStatsDto {

    private String scope; // 통계 범위
    private int days; // 최근 N일 기준
    private long totalLikes;  
    private long totalDislikes;
    private long net; // likes - dislikes : 전체적인 품질 지표

    // 모델별 피드백 통계 : 이 모델이 만든 응답들의 평가 종합
    private List<ModelStat> byModel;  

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ModelStat { 
        private String model;
        private long likes;
        private long dislikes;
        private long net;
    }
    
    // 문제 응답 Top 리스트 : 싫어요를 많이 받은 문제 응답 Top N
    private List<TopMessage> topDislikedMessages; 
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopMessage {
        private Long chatId;
        private String model;
        private long dislikes;
        private long likes;
        private long net;
    }
}
