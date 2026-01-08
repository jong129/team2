package dev.jpa.team2.chatbot.message;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    // Oracle CLOB 검색: DBMS_LOB.INSTR 사용
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
    
    // 관리자: 메시지 갯수
    @Query("""
        SELECT m.sessionId, COUNT(m)
        FROM ChatMessage m
        WHERE m.sessionId IN :sessionIds
        GROUP BY m.sessionId
        """)
    List<Object[]> countBySessionIds(@Param("sessionIds") List<Long> sessionIds);
    
    long countBySessionId(Long sessionId);

    List<ChatMessage> findTop4BySessionIdOrderByCreatedAtAsc(Long sessionId);

}
