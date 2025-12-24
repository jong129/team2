package dev.jpa.team2.chatbot;

import dev.jpa.team2.chatbot.RagResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RagResultRepository
        extends JpaRepository<RagResult, Long> {

    List<RagResult> findBySessionId(Long sessionId);
}
