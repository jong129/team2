package dev.jpa.team2.chatbot.embeddingchunk;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmbeddingChunkRepository extends JpaRepository<EmbeddingChunk, Long> {

    @Query("""
        select e.chunkId, e.fileId, e.chunkText, e.vectorData
        from EmbeddingChunk e
    """)
    List<Object[]> findAllForSimilarity();
}
