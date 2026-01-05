package dev.jpa.team2.chatbot.session;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionIdAndMemberId(Long sessionId, Long memberId);

    Optional<ChatSession> findTopByMemberIdAndSessionStatusOrderByLastMessageAtDesc(Long memberId, String sessionStatus);

    List<ChatSession> findByMemberIdAndSessionStatusOrderByLastMessageAtDesc(Long memberId, String sessionStatus);
    
 // 관리자: 전체 세션 조회(필터 포함)
    @Query("""
        SELECT s
        FROM ChatSession s
        WHERE (:status IS NULL OR s.sessionStatus = :status)
          AND (:memberId IS NULL OR s.memberId = :memberId)
          AND (:q IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:from IS NULL OR s.lastMessageAt >= :from)
          AND (:to IS NULL OR s.lastMessageAt <= :to)
        ORDER BY s.lastMessageAt DESC
    """)
    Page<ChatSession> adminFindSessions(
        @Param("status") String status,
        @Param("memberId") Long memberId,
        @Param("q") String q,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );

    // 관리자: 단건 조회
    ChatSession findBySessionId(Long sessionId);

}