package dev.jpa.team2.chatbot;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/embeddings")
public class EmbeddingChunkController {

    private final EmbeddingChunkService service;

    @PostMapping
    public ResponseEntity<EmbeddingChunkDto> create(
            @RequestBody EmbeddingChunkDto dto) {

        return ResponseEntity.ok(
            service.create(dto)
        );
    }
}
