package dev.jpa.team2.chatbot.domain.embeddingchunk;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/embeddings")
public class EmbeddingChunkController {

    private final EmbeddingChunkService service;

    @PostMapping
    public ResponseEntity<EmbeddingChunkDto.CreateResponse> create(
            @RequestBody EmbeddingChunkDto.CreateRequest req) {

        return ResponseEntity.ok(service.create(req));
    }
}
