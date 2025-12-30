package dev.jpa.team2.checklist.pre;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
