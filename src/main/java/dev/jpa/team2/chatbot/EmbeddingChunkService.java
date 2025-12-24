package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmbeddingChunkService {

    private final EmbeddingChunkRepository repository;
    private final FastApiLlmService llmService;   // ✅ OpenAiService → FastApiLlmService
    private final ObjectMapper objectMapper;      // ✅ List<Double>를 JSON 문자열로 바꾸려고 필요

    public EmbeddingChunkDto create(EmbeddingChunkDto dto) {

        // ✅ FastAPI에서 임베딩은 List<Double>
        List<Double> vector = llmService.embedding(dto.getChunkText());

        // ✅ DB에는 String(CLOB)으로 저장해야 하니 JSON 문자열로 변환
        String vectorJson;
        try {
            vectorJson = objectMapper.writeValueAsString(vector); // 예: [0.1,0.2,0.3]
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Embedding vector JSON 변환 실패", e);
        }

        EmbeddingChunk entity = new EmbeddingChunk();
        entity.setFileId(dto.getFileId());
        entity.setChunkText(dto.getChunkText());
        entity.setVectorData(vectorJson); // ✅ vectorData에 JSON 저장
        entity.setCreatedAt(LocalDateTime.now());

        EmbeddingChunk saved = repository.save(entity);

        dto.setChunkId(saved.getChunkId());
        return dto;
    }
}
