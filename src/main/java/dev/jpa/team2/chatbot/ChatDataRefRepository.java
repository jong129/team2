package dev.jpa.team2.chatbot;

import dev.jpa.team2.chatbot.ChatDataRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatDataRefRepository
        extends JpaRepository<ChatDataRef, Long> {

    List<ChatDataRef> findByChatId(Long chatId);
}
