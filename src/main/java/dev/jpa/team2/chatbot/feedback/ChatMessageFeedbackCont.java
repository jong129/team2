package dev.jpa.team2.chatbot.feedback;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.jpa.team2.chatbot.AuthSessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/messages")
public class ChatMessageFeedbackCont {

    private final ChatMessageFeedbackService feedbackService;

    // POST /api/chat/messages/{chatId}/feedback  { liked: true/false }
    @PostMapping("/{chatId}/feedback")
    public ResponseEntity<ChatMessageFeedbackDto> feedback(
        @PathVariable Long chatId,
        @RequestBody ChatMessageFeedbackDto dto,
        HttpSession session
    ) {
        Long memberId = AuthSessionUtil.requireMemberId(session);
        boolean liked = Boolean.TRUE.equals(dto.getLiked());

        ChatMessageFeedbackDto result =
            feedbackService.upsertOrToggle(memberId, chatId, liked);

        return ResponseEntity.ok(result);
    }

}
