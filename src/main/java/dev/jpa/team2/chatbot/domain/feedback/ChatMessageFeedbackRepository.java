package dev.jpa.team2.chatbot.domain.feedback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatMessageFeedbackRepository extends JpaRepository<ChatMessageFeedback, Long> {

    Optional<ChatMessageFeedback> findByMemberIdAndChatId(Long memberId, Long chatId);

    long countByChatIdAndValue(Long chatId, Integer value);

    // ✅ 전체 합계(기간) - "단일 row"라도 List<Object[]>로 받는게 제일 안전
    @Query("""
        select
            sum(case when f.value = 1 then 1 else 0 end),
            sum(case when f.value = -1 then 1 else 0 end)
        from ChatMessageFeedback f
        where f.createdAt >= :since
    """)
    List<Object[]> totalsAll(@Param("since") LocalDateTime since);

    // 모델별 집계(기간) - chat_message.model 기준
    @Query("""
        select
            coalesce(m.model, 'UNKNOWN'),
            sum(case when f.value = 1 then 1 else 0 end),
            sum(case when f.value = -1 then 1 else 0 end)
        from ChatMessageFeedback f
        join ChatMessage m on m.chatId = f.chatId
        where f.createdAt >= :since
        group by m.model
        order by
            (sum(case when f.value = 1 then 1 else 0 end) - sum(case when f.value = -1 then 1 else 0 end)) asc
    """)
    List<Object[]> byModelAll(@Param("since") LocalDateTime since);

    // 싫어요 많은 메시지 Top N (기간)
    @Query("""
        select
            f.chatId,
            coalesce(m.model, 'UNKNOWN'),
            sum(case when f.value = -1 then 1 else 0 end),
            sum(case when f.value = 1 then 1 else 0 end)
        from ChatMessageFeedback f
        join ChatMessage m on m.chatId = f.chatId
        where f.createdAt >= :since
        group by f.chatId, m.model
        order by
            sum(case when f.value = -1 then 1 else 0 end) desc
    """)
    List<Object[]> topDislikedAll(@Param("since") LocalDateTime since, Pageable pageable);
}
