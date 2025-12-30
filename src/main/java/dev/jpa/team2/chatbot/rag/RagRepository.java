package dev.jpa.team2.chatbot.rag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RagRepository
        extends JpaRepository<Rag, Long> {

    List<Rag> findBySessionId(Long sessionId);
}
