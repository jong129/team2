package dev.jpa.team2.chatbot.feedback;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageFeedbackRepository extends JpaRepository<ChatMessageFeedback, Long> {
    Optional<ChatMessageFeedback> findByMemberIdAndChatId(Long memberId, Long chatId);
    long countByChatIdAndValue(Long chatId, Integer value);
}
