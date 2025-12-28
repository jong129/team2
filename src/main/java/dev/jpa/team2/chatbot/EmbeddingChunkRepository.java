package dev.jpa.team2.chatbot;

import dev.jpa.team2.chatbot.EmbeddingChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmbeddingChunkRepository extends JpaRepository<EmbeddingChunk, Long> {

    List<EmbeddingChunk> findByFileId(Long fileId);
}
