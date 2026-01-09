package dev.jpa.team2.chatbot.message;

import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long chatId;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    // ✅ 피드백 관련(집계)
    private Integer likeCount;
    private Integer dislikeCount;

    // ✅ "내가" 이 메시지에 남긴 값: null/0/1/-1 중 택1 (프론트 표시용)
    // 여기서는 1(좋아요), -1(싫어요), null(미평가)로 쓰자
    private Integer myFeedback;

    public static ChatMessageDto from(ChatMessage m) {
        return new ChatMessageDto(
            m.getChatId(),
            m.getRole(),
            m.getContent(),
            m.getCreatedAt(),
            m.getLikeCount(),
            m.getDislikeCount(),
            null // myFeedback은 서비스에서 채워넣을 거야
        );
    }
}
