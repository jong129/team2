package dev.jpa.team2.chatbot;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/messages")
public class ChatHistoryController {

    private final ChatHistoryService service;

    @PostMapping
    public ResponseEntity<ChatHistory> save(
            @RequestBody ChatHistoryDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }
}
