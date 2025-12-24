package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryRepository repository;

    public ChatHistory save(ChatHistoryDto dto) {
        ChatHistory chat = new ChatHistory(
            dto.getSessionId(),
            dto.getQuestion(),
            dto.getAnswer()
        );
        return repository.save(chat);
    }
}

