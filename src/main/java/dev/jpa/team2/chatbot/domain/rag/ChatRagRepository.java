package dev.jpa.team2.chatbot.domain.rag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRagRepository extends JpaRepository<ChatRag, Long> {
    // 특정 세션의 RAG 로그 목록 조회용
    List<ChatRag> findBySessionId(Long sessionId);
}
