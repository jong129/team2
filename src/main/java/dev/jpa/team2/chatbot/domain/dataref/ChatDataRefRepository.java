package dev.jpa.team2.chatbot.domain.dataref;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatDataRefRepository extends JpaRepository<ChatDataRef, Long> {
    List<ChatDataRef> findByMemberIdAndSessionIdOrderByCreatedAtDesc(Long memberId, Long sessionId);
}
