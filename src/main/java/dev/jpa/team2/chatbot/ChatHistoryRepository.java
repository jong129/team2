package dev.jpa.team2.chatbot;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    // ✅ "내 대화"에서 키워드 검색 (세션과 조인)
    @Query("""
        select m
        from ChatHistory m, ChatSession s
        where m.sessionId = s.sessionId
          and s.memberId = :memberId
          and s.sessionStatus <> 'DELETED'
          and m.content like concat('%', :keyword, '%')
        order by m.createdAt desc
        """)
        List<ChatHistory> searchMyMessages(Long memberId, String keyword, Pageable pageable);

}
