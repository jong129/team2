package dev.jpa.team2.chatbot.domain.feedback;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jpa.team2.chatbot.domain.message.ChatMessage;
import dev.jpa.team2.chatbot.domain.message.ChatMessageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageFeedbackService {

    private final ChatMessageRepository chatMessageRepo;
    private final ChatMessageFeedbackRepository feedbackRepo;

    /**
     * 규칙
     * - ASSISTANT 메시지만 평가 가능
     * - (memberId, chatId) 1개만 존재
     * - 같은 값 다시 누르면 취소(삭제)
     * - 다른 값 누르면 변경(UPDATE)
     * - CHAT_MESSAGE.like_count/dislike_count는 즉시 반영
     */
    public ChatMessageFeedbackDto upsertOrToggle(
        Long memberId,
        Long chatId,
        boolean liked
    ) {
        int newValue = liked ? 1 : -1;

        ChatMessage msg = chatMessageRepo.findById(chatId)
            .orElseThrow(() -> new IllegalArgumentException("메시지가 없습니다. chatId=" + chatId));

        if (!"ASSISTANT".equalsIgnoreCase(msg.getRole())) {
            throw new IllegalStateException("AI 답변만 평가할 수 있습니다.");
        }

        var opt = feedbackRepo.findByMemberIdAndChatId(memberId, chatId);

        Integer myFeedback = null;

        if (opt.isEmpty()) {
            feedbackRepo.save(ChatMessageFeedback.of(chatId, memberId, newValue));
            applyDelta(msg, newValue, +1);
            myFeedback = newValue;
        } else {
            ChatMessageFeedback exist = opt.get();
            int oldValue = exist.getValue();

            if (oldValue == newValue) {
                feedbackRepo.delete(exist);
                applyDelta(msg, oldValue, -1);
                myFeedback = null;
            } else {
                exist.setValue(newValue);
                exist.setUpdatedAt(LocalDateTime.now());
                applyDelta(msg, oldValue, -1);
                applyDelta(msg, newValue, +1);
                myFeedback = newValue;
            }
        }

        return ChatMessageFeedbackDto.builder()
            .chatId(chatId)
            .likeCount(msg.getLikeCount())
            .dislikeCount(msg.getDislikeCount())
            .myFeedback(myFeedback)
            .build();
    }


    private void applyDelta(ChatMessage msg, int value, int delta) {
        if (value == 1) {
            msg.setLikeCount(Math.max(0, (msg.getLikeCount() == null ? 0 : msg.getLikeCount()) + delta));
        } else if (value == -1) {
            msg.setDislikeCount(Math.max(0, (msg.getDislikeCount() == null ? 0 : msg.getDislikeCount()) + delta));
        }
        // msg는 트랜잭션 종료 시 자동 flush
    }
}
