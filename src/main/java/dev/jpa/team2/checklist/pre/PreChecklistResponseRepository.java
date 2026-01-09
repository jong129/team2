package dev.jpa.team2.checklist.pre;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.jpa.team2.checklist.model.ChecklistResponse;

/**
 * CHECKLIST_RESPONSE 조회/저장 Repository (PRE 공용)
 * - (sessionId, itemId) 기준으로 upsert 하기 위해 사용
 */
public interface PreChecklistResponseRepository extends JpaRepository<ChecklistResponse, Long> {

    /**
     * 한 세션에서 한 항목에 대한 응답 1개 조회 (UQ: SESSION_ID + ITEM_ID)
     */
    Optional<ChecklistResponse> findBySession_SessionIdAndItem_ItemId(Long sessionId, Long itemId);

    List<ChecklistResponse> findBySession_SessionId(Long sessionId);
    
 // ✅ 추가: 체크 응답이 하나라도 있는지
    boolean existsBySession_SessionId(Long sessionId);

    /**
     * (E) 세션 초기화: 해당 세션의 모든 응답 상태를 NOT_DONE으로 일괄 변경
     * @return 업데이트된 row 수
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ChecklistResponse r
           set r.checkStatus = 'NOT_DONE'
         where r.session.sessionId = :sessionId
    """)
    int resetAllToNotDone(@Param("sessionId") Long sessionId);
}
