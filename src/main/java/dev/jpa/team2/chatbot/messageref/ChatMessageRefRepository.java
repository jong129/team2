package dev.jpa.team2.chatbot.messageref;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRefRepository extends JpaRepository<ChatMessageRef, Long> {

    List<ChatMessageRef> findByChatIdOrderByCreatedAtAsc(Long chatId);

    // 전체 통계: 최근 N일 평균 점수/총건수
    @Query("""
        select count(r), avg(r.score)
        from ChatMessageRef r
        where r.createdAt >= :since
    """)
    Object[] statsAll(@Param("since") LocalDateTime since);

    // 회원별 통계: chat_message -> chat_session -> member_id 로 조인해서 회원것만
    @Query("""
        select count(r), avg(r.score)
        from ChatMessageRef r
        join ChatMessage m on m.chatId = r.chatId
        join ChatSession s on s.sessionId = m.sessionId
        where s.memberId = :memberId
          and r.createdAt >= :since
    """)
    Object[] statsMy(@Param("memberId") Long memberId, @Param("since") LocalDateTime since);

    // 전체 Top chunk
    @Query("""
        select r.chunkId, count(r), avg(r.score)
        from ChatMessageRef r
        where r.createdAt >= :since
        group by r.chunkId
        order by count(r) desc
    """)
    List<Object[]> topChunksAll(@Param("since") LocalDateTime since, Pageable pageable);

    // 회원 Top chunk
    @Query("""
        select r.chunkId, count(r), avg(r.score)
        from ChatMessageRef r
        join ChatMessage m on m.chatId = r.chatId
        join ChatSession s on s.sessionId = m.sessionId
        where s.memberId = :memberId
          and r.createdAt >= :since
        group by r.chunkId
        order by count(r) desc
    """)
    List<Object[]> topChunksMy(@Param("memberId") Long memberId, @Param("since") LocalDateTime since, Pageable pageable);
}
