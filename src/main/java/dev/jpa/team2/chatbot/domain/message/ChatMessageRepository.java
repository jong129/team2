package dev.jpa.team2.chatbot.domain.message;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    @Query(value = """
        SELECT m.*
        FROM chat_message m
        JOIN chat_session s ON s.session_id = m.session_id
        WHERE s.member_id = :memberId
          AND DBMS_LOB.INSTR(m.content, :keyword) > 0
        ORDER BY m.created_at DESC
    """, nativeQuery = true)
    List<ChatMessage> searchMyMessages(
        @Param("memberId") Long memberId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    @Query("""
        SELECT m.sessionId, COUNT(m)
        FROM ChatMessage m
        WHERE m.sessionId IN :sessionIds
        GROUP BY m.sessionId
        """)
    List<Object[]> countBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    long countBySessionId(Long sessionId);

    List<ChatMessage> findTop4BySessionIdOrderByCreatedAtAsc(Long sessionId);

    // --------------------------------------------
    // ✅ usage 통계 (ROLE=ASSISTANT 기준)
    // --------------------------------------------

    /**
     * summary: [total_requests, total_tokens, avg_tokens, avg_latency_ms]
     * - avg는 0값 제외 (과거 데이터 TOKENS/LATENCY=0 보정)
     *
     * ✅ 단건 반환(Object[]) 대신 List<Object[]>로 받는 게 Oracle/JPA에서 더 안전함
     */
    @Query(value = """
        SELECT
            COUNT(*) AS total_requests,
            NVL(SUM(NVL(TOKENS_TOTAL, NVL(TOKENS_IN,0) + NVL(TOKENS_OUT,0))), 0) AS total_tokens,
            AVG(NULLIF(NVL(TOKENS_TOTAL, NVL(TOKENS_IN,0) + NVL(TOKENS_OUT,0)), 0)) AS avg_tokens,
            AVG(NULLIF(LATENCY_MS, 0)) AS avg_latency_ms
        FROM CHAT_MESSAGE
        WHERE ROLE = 'ASSISTANT'
          AND CREATED_AT >= (SYSDATE - :days)
    """, nativeQuery = true)
    List<Object[]> usageSummary(@Param("days") int days);

    /**
     * byModel rows: [model, requests, tokens, avg_tokens, avg_latency_ms]
     */
    @Query(value = """
        SELECT
            NVL(MODEL, 'UNKNOWN') AS model,
            COUNT(*) AS requests,
            NVL(SUM(NVL(TOKENS_TOTAL, NVL(TOKENS_IN,0) + NVL(TOKENS_OUT,0))), 0) AS tokens,
            AVG(NULLIF(NVL(TOKENS_TOTAL, NVL(TOKENS_IN,0) + NVL(TOKENS_OUT,0)), 0)) AS avg_tokens,
            AVG(NULLIF(LATENCY_MS, 0)) AS avg_latency_ms
        FROM CHAT_MESSAGE
        WHERE ROLE = 'ASSISTANT'
          AND CREATED_AT >= (SYSDATE - :days)
        GROUP BY NVL(MODEL, 'UNKNOWN')
        ORDER BY tokens DESC
    """, nativeQuery = true)
    List<Object[]> usageByModel(@Param("days") int days);

}
