package dev.jpa.team2.chatbot.domain.rag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRagRepository
        extends JpaRepository<ChatRag, Long> {

    List<ChatRag> findBySessionId(Long sessionId);
}
