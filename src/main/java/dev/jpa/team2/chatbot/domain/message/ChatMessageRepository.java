package dev.jpa.team2.chatbot.domain.message;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // 세션 대화 전체 로딩(시간순)
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
    
    // 세션 메시지 bulk 삭제
    void deleteBySessionId(Long sessionId);
    
    // CLOB content에서 DBMS_LOB.INSTR로 키워드 검색 + 세션 소유자(memberId) 조인 검증
    // native + DMNS_LOB.INSTR 쓰는 이유 : Oracle에서 CLOB 컬럼은 일반적인 LIKE '%keyword%'가 제약/비효율이 있을 수 있음
    // join하는 이유 : chat_message만 검색하면 내 세션이 아닌 메시지도 검색될 수 있음
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
    
    // 관리자 리스트에서 N+1 방지용 세션별 메시지 수 벌크 집계
    @Query("""
        SELECT m.sessionId, COUNT(m)
        FROM ChatMessage m
        WHERE m.sessionId IN :sessionIds
        GROUP BY m.sessionId
        """)
    List<Object[]> countBySessionIds(@Param("sessionIds") List<Long> sessionIds);
    
    // 단일 세션 메시지 수 카운트
    long countBySessionId(Long sessionId);
    
    // 세션의 앞부분 4개 메시지 가져오기
    List<ChatMessage> findTop4BySessionIdOrderByCreatedAtAsc(Long sessionId);

    // --------------------------------------------
    // usage 통계 (ROLE=ASSISTANT 기준)
    // --------------------------------------------

    // summary: [total_requests, total_tokens, avg_tokens, avg_latency_ms]
    // avg는 0값 제외 (과거 데이터 TOKENS/LATENCY=0 보정)
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

    
    // 최근 N일 기준으로 모델별 응답 수, 토큰 합, 평균 토큰, 평균 지연
    // 토큰을 많이 쓴 모델 순으로 정렬
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
