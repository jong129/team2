package dev.jpa.team2.chatbot;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByMemberId(Long memberId);
}
