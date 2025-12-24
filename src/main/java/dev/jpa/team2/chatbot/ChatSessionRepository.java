package dev.jpa.team2.chatbot;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findTopByMemberIdAndSessionStatusOrderByLastMessageAtDesc(
        Long memberId, String sessionStatus
    );

    List<ChatSession> findByMemberIdAndSessionStatusOrderByLastMessageAtDesc(
        Long memberId, String sessionStatus
    );

    Optional<ChatSession> findBySessionIdAndMemberId(Long sessionId, Long memberId);
}
