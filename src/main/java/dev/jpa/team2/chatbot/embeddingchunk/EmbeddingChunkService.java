package dev.jpa.team2.chatbot.embeddingchunk;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jpa.team2.chatbot.FastApiLlmService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmbeddingChunkService {

    private final EmbeddingChunkRepository repository;
    private final FastApiLlmService llmService;
    private final ObjectMapper objectMapper;

    public EmbeddingChunkDto.CreateResponse create(EmbeddingChunkDto.CreateRequest req) {

        List<Double> vector = llmService.embedding(req.getChunkText());

        String vectorJson;
        try {
            vectorJson = objectMapper.writeValueAsString(vector);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Embedding vector JSON 변환 실패", e);
        }

        EmbeddingChunk entity = new EmbeddingChunk();
        entity.setFileId(req.getFileId());
        entity.setChunkText(req.getChunkText());
        entity.setVectorData(vectorJson);
        entity.setCreatedAt(LocalDateTime.now());

        EmbeddingChunk saved = repository.save(entity);

        return EmbeddingChunkDto.CreateResponse.builder()
            .success(true)
            .chunkId(saved.getChunkId())
            .build();
    }
}
