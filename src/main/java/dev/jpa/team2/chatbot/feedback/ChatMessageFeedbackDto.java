package dev.jpa.team2.chatbot.feedback;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageFeedbackDto {

    // ===== 요청용 =====
    // true = 좋아요, false = 싫어요
    private Boolean liked;

    // ===== 공통/응답용 =====
    private Long chatId;

    // 집계 값 (CHAT_MESSAGE 기준)
    private Integer likeCount;
    private Integer dislikeCount;

    // 내가 남긴 평가
    //  1  = 좋아요
    // -1  = 싫어요
    // null = 평가 안 함
    private Integer myFeedback;
}
