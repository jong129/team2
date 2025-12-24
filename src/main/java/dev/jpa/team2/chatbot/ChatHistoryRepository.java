package dev.jpa.team2.chatbot;

import dev.jpa.team2.chatbot.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findBySessionId(Long sessionId);
}
