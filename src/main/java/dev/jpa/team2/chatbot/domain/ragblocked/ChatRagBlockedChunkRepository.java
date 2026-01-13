package dev.jpa.team2.chatbot.domain.ragblocked;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// 차단 목록 조회/검색/확성 chunkId 목록 추출

public interface ChatRagBlockedChunkRepository extends JpaRepository<ChatRagBlockedChunk, Long> {
    // 이 chunk가 이미 차단 이력이 있는지(존재 여부) 확인하는 upsert
    Optional<ChatRagBlockedChunk> findByChunkId(Long chunkId);
    
    // RAG 검색 제외용 : 활성 차단 chunkId만 뽑아서 Python/검색로직에 전달하기 좋게 만든 쿼리
    @Query("select b.chunkId from ChatRagBlockedChunk b where b.isActive = 1")
    List<Long> findActiveChunkIds();
    
    // 관리자 페이지에서 활성/비활성 목록을 최신순으로 조회
    List<ChatRagBlockedChunk> findByIsActiveOrderByCreatedAtDesc(Integer isActive);
}
