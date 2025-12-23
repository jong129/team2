package dev.jpa.team2.chatbot;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmbeddingChunkService {

    private final EmbeddingChunkRepository repository;
    private final OpenAiService openAiService;
    
    public EmbeddingChunkDto create(EmbeddingChunkDto dto) {

        String vector = openAiService.embedding(dto.getChunkText());

        EmbeddingChunk entity = new EmbeddingChunk();
        entity.setFileId(dto.getFileId());
        entity.setChunkText(dto.getChunkText());
        entity.setVectorData(vector);
        entity.setCreatedAt(LocalDateTime.now());

        EmbeddingChunk saved = repository.save(entity);

        dto.setChunkId(saved.getChunkId());
        return dto;
    }
}
