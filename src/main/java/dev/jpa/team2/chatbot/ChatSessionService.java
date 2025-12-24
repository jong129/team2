package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository repository;

    public ChatSession create(ChatSessionCreateDto dto) {
        ChatSession session = new ChatSession();
        session.setMemberId(dto.getMemberId());
        session.setTitle(dto.getTitle());
        session.setSessionStatus("ACTIVE");
        session.setStartTime(LocalDateTime.now());
        return repository.save(session);
    }
}
