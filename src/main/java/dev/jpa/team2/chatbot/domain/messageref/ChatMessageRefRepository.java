package dev.jpa.team2.chatbot.domain.messageref;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.chatbot.domain.message.ChatMessage;

public interface ChatMessageRefRepository extends JpaRepository<ChatMessageRef, Long> {

    // ✅ 근거 조회: rank 우선 추천
    List<ChatMessageRef> findByChatIdOrderByRankNoAsc(Long chatId);

    // (필요하면 유지)
    List<ChatMessageRef> findByChatIdOrderByCreatedAtAsc(Long chatId);

    // -----------------------------
    // ✅ 통계(전체) - avgScore null 방지 (0으로 내려줌)
    // -----------------------------
    @Query("""
        select
          count(r),
          coalesce(avg(r.score), 0)
        from ChatMessageRef r
        where r.createdAt >= :since
    """)
    Object[] statsAll(@Param("since") LocalDateTime since);

    // ✅ 전체 Top chunk (dislike 연관 포함) - avgScore null 방지
    @Query("""
        select
          r.chunkId,
          count(r),
          coalesce(avg(r.score), 0),
          sum(coalesce(m.dislikeCount, 0)),
          sum(case when coalesce(m.dislikeCount, 0) >= :badN then 1 else 0 end)
        from ChatMessageRef r
        join ChatMessage m on m.chatId = r.chatId
        where r.createdAt >= :since
          and m.role = 'ASSISTANT'
        group by r.chunkId
        order by
          sum(case when coalesce(m.dislikeCount, 0) >= :badN then 1 else 0 end) desc,
          sum(coalesce(m.dislikeCount, 0)) desc,
          count(r) desc
    """)
    List<Object[]> topChunksAll(
        @Param("since") LocalDateTime since,
        @Param("badN") int badN,
        Pageable pageable
    );

    // -----------------------------
    // ✅ 나쁜 답변 기반 "문제 chunk 후보"(전체)
    // - dislike_count >= badN 인 답변만 대상으로 chunk 집계
    // - avgScore null 방지
    // -----------------------------
    @Query("""
        select
          r.chunkId,
          count(r),
          coalesce(avg(r.score), 0),
          sum(coalesce(m.dislikeCount, 0)),
          count(distinct m.chatId)
        from ChatMessageRef r
        join ChatMessage m on m.chatId = r.chatId
        where r.createdAt >= :since
          and m.role = 'ASSISTANT'
          and coalesce(m.dislikeCount, 0) >= :badN
        group by r.chunkId
        order by sum(coalesce(m.dislikeCount, 0)) desc, count(r) desc
    """)
    List<Object[]> badChunksAll(
        @Param("since") LocalDateTime since,
        @Param("badN") int badN,
        Pageable pageable
    );
}
