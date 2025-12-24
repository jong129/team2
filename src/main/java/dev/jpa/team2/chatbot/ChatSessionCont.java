package dev.jpa.team2.chatbot;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/sessions")
public class ChatSessionCont {

    private final ChatSessionService service;

    @PostMapping
    public ResponseEntity<ChatSession> create(
            @RequestBody ChatSessionCreateDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }
}
