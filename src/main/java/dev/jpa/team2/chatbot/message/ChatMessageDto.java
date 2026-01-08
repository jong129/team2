package dev.jpa.team2.chatbot.message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // ✅ "내가" 이 메시지에 남긴 값: 1 / -1 / null
    private Integer myFeedback;

    // ==========================
    // ✅ 추가: 추천 후속 질문 (AI 응답에만 존재)
    // ==========================
    private List<String> followUpQuestions = new ArrayList<>();

    public static ChatMessageDto from(ChatMessage m) {

        ChatMessageDto dto = new ChatMessageDto(
            m.getChatId(),
            m.getRole(),
            m.getContent(),
            m.getCreatedAt(),
            m.getLikeCount(),
            m.getDislikeCount(),
            null,   // myFeedback은 서비스에서 채워넣음
            new ArrayList<>() // followupquestions
        );

        // ✅ ASSISTANT 메시지인 경우만 추천질문 세팅
        if ("ASSISTANT".equalsIgnoreCase(m.getRole())) {
            List<String> fu = new ArrayList<>();
            if (m.getSuggestQ1() != null && !m.getSuggestQ1().isBlank()) fu.add(m.getSuggestQ1());
            if (m.getSuggestQ2() != null && !m.getSuggestQ2().isBlank()) fu.add(m.getSuggestQ2());
            if (m.getSuggestQ3() != null && !m.getSuggestQ3().isBlank()) fu.add(m.getSuggestQ3());
            dto.setFollowUpQuestions(fu);
        }

        return dto;
    }
}
