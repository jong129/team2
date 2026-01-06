package dev.jpa.team2.chatbot.ragblocked;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRagBlockedChunkRepository extends JpaRepository<ChatRagBlockedChunk, Long> {

    Optional<ChatRagBlockedChunk> findByChunkId(Long chunkId);

    @Query("select b.chunkId from ChatRagBlockedChunk b where b.isActive = 1")
    List<Long> findActiveChunkIds();

    List<ChatRagBlockedChunk> findByIsActiveOrderByCreatedAtDesc(Integer isActive);
}
