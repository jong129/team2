package dev.jpa.team2.chatbot.domain.messageref;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRefRepository extends JpaRepository<ChatMessageRef, Long> {
    // ===== 기본 조회 =====
    // 근거 조회: 추천 순서(rankNo)
    List<ChatMessageRef> findByChatIdOrderByRankNoAsc(Long chatId);

    // 시간순 조회
    List<ChatMessageRef> findByChatIdOrderByCreatedAtAsc(Long chatId);

    // ===== 통계(전체) - avgScore null 방지 (0으로 내려줌)  =====
    // 기간 내 전체 근거 개수 + 평균 유사도 (coalesce(avg(...),0) : 평균이 null이면 0으로 처리)
    @Query("""
        select
          count(r),
          coalesce(avg(r.score), 0)
        from ChatMessageRef r
        where r.createdAt >= :since
    """)
    Object[] statsAll(@Param("since") LocalDateTime since);

    // 전체 Top chunk (dislike 연관 포함) : 전체에서 나쁜 답변 연관까지 같이 본 랭킹
    // ChatMessageRef r를 ChatMessage m과 join
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

    // 나쁜 답변 기반 문제 chunk 후보 (전체) : 나쁜 답변만 필터링해서 후보를 뽑는 랭킹
    // - dislike_count >= badN 인 답변만 대상으로 chunk 집계
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
