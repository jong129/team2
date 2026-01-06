package dev.jpa.team2.chatbot.feedback;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatFeedbackStatsDto {

    private String scope; // "ALL"
    private int days;

    private long totalLikes;
    private long totalDislikes;
    private long net; // likes - dislikes

    private List<ModelStat> byModel;
    private List<TopMessage> topDislikedMessages;

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
