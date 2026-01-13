package dev.jpa.team2.chatbot.domain.feedback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatMessageFeedbackRepository extends JpaRepository<ChatMessageFeedback, Long> {
  
    // 내가 이 메시지에 뭘 눌렀는지 조회
    Optional<ChatMessageFeedback> findByMemberIdAndChatId(Long memberId, Long chatId);
    
    // 특정 메시지의 좋아요/싫어요 개수
    long countByChatIdAndValue(Long chatId, Integer value);

    // ===== 통계 쿼리 (기간 since 기준) =====
    // 기간 내 전체 좋아요/싫어요 합계
    @Query("""
        select
            sum(case when f.value = 1 then 1 else 0 end),
            sum(case when f.value = -1 then 1 else 0 end)
        from ChatMessageFeedback f
        where f.createdAt >= :since
    """)
    List<Object[]> totalsAll(@Param("since") LocalDateTime since);

    // 모델별 좋아요/싫어요 합계 : ChatMessageFeedback f와 ChatMessage m을 chatId로 join
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

    // 기간 내 싫어요가 많은 메시지를 상위 N개 : Pageable로 Top N 제한
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
    
    // 세션 메시지 로딩 때 내 피드백을 chatId들에 대해 일괄 조회 : [chatId, value] 배열 리스트 반환
    @Query("""
        select f.chatId, f.value
        from ChatMessageFeedback f
        where f.memberId = :memberId
          and f.chatId in :chatIds
    """)
    List<Object[]> findMyFeedbackByChatIds(@Param("memberId") Long memberId, @Param("chatIds") List<Long> chatIds);

}
