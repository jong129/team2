package dev.jpa.team2.chatbot.domain.embeddingchunk;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

// 프론트에서 임베딩 생성 요청을 받는 REST API
// 요청 받기 -> service.create(req) 호출 -> 그대로 반환

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
