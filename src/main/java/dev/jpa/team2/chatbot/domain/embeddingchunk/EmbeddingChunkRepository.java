package dev.jpa.team2.chatbot.domain.embeddingchunk;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmbeddingChunkRepository extends JpaRepository<EmbeddingChunk, Long> {
  
    // 모든 청크의 chunkId/fileId/chunkText/vectorData를 뽑아오는 쿼리
    @Query("""
        select e.chunkId, e.fileId, e.chunkText, e.vectorData
        from EmbeddingChunk e
    """)
    List<Object[]> findAllForSimilarity();
}
